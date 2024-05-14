package parser;

import java.util.List;

import codegen.TableStack;
import codegen.FunctionTableStack;
import codegen.SymbolTable;
import codegen.VarInfo;

import java.util.ArrayList;

import tokenizer.Token;
import tokenizer.TokenType;
import tokenizer.Tokenizer;

public class Parser 
{
     private Scope globalScope = new Scope(null);
     private List<Token> tokenList = new ArrayList<Token>();
     private TableStack tableStack = new TableStack();
     private FunctionTableStack fTableStack = new FunctionTableStack();
     private LiteralTable litTable = new LiteralTable();
     private int tokenPos;

     public Parser(List<Token> tokens)
     {
        tokenList = tokens;
     }

     public boolean parseProgram() throws ParseException
     {
        tokenPos = 0;
        boolean exit = false;

        tableStack.push(new SymbolTable());
        fTableStack.pushEmptyTable();

        while (!exit && tokenPos < tokenList.size())
        {
            globalScope.addStatement(parseStatement(null));
        }

        return !exit;
     }

     public void printProgram()
     {
        globalScope.printStatement();
     }

     public Scope getProgram()
     {
        return globalScope;
     }

     public TableStack getTableStack()
     {
        return tableStack;
     }

     public LiteralTable getLitTable()
     {
        return litTable;
     }

     private Statement parseStatement(String functionName) throws ParseException
     {
        try
        {
            if (peek() != null)
            {
                switch( peek().getType() )
                {
                    case OPEN_BRACE:
                    return parseScope(functionName);

                    case TYPE:
                    if (peek(2) != null && peek(2).getType() == TokenType.EQUALS)
                    {
                        return parseDeclaration();
                    }

                    if (peek(2) != null && peek(2).getType() == TokenType.OPEN_PAREN)
                    {
                        return parseFunctionDeclaration();
                    }

                    case IDENTIFIER:
                    return parseReassignment();
                    
                    case RETURN:
                    return parseReturnStatement(functionName);

                    case PRINT:
                    return parsePrintStatement();

                    case EXIT:
                    return parseExitStatement();

                    default:
                    throw new ParseException("Invalid use of token: " + 
                                            peek().getValue(), peek());
                }
            }

            throw new ParseException("Expected statement, got EOF");
        }
        catch (ParseException exception)
        {
            throw exception;
        }
     }

     private Scope parseScope(String functionName) throws ParseException
     {
        Scope newScope = new Scope(functionName);
        tableStack.push(new SymbolTable());
        fTableStack.pushEmptyTable();
        FunctionDeclaration function;
        List<Parameter> params;

        // set up symbol table with parameters if this is a function
        if (functionName != null)
        {
            function = fTableStack.getFunctionDeclaration(functionName);

            if (function == null)
            {
                throw new ParseException("Could not get function info for function '" + functionName + "'", peek());
            }

            params = function.getParams();

            for (int i = 0; i < params.size(); i++)
            {
                if (!tableStack.identifierInUse(params.get(i).getIdentifier()))
                {
                    tableStack.peek().addIdentifier(params.get(i).getType(), params.get(i).getIdentifier());
                }
            }
        }

        if (peek().getType() == TokenType.OPEN_BRACE)
        {
            consume();

            while (peek() != null && peek().getType() != TokenType.CLOSE_BRACE)
            {
                newScope.addStatement(parseStatement(functionName));
            }
            
            if (peek() == null)
            {
                throw new ParseException("Unexpected EOF, expected '}' to close scope", peek());
            }

            consume();
            tableStack.pop();
            fTableStack.pop();

            return newScope;
        }
        else
        {
            throw new ParseException("Expected '{', got " + peek().getValue(), peek());
        }
     }

     private Declaration parseDeclaration() throws ParseException
     {
        Token typeToken;
        Token identifierToken;
        Declaration newDeclaration;

        try 
        {
            if (peek() != null) 
            {
                if (peek().getType() == TokenType.TYPE)
                {
                    typeToken = consume();

                    if (peek().getType() == TokenType.IDENTIFIER) 
                    {
                        identifierToken = consume();

                        if (peek() != null && peek().getType() == TokenType.EQUALS) 
                        {
                            consume();
                            newDeclaration = new Declaration(typeToken, identifierToken, parseExpression(typeToken.getValue(), peek()));
                            
                            if ( peek() != null && peek().getType() == TokenType.SEMICOLON ) 
                            {
                                consume();
                                
                                if (!tableStack.peek().addIdentifier(typeToken.getValue(), identifierToken.getValue()))
                                {
                                    throw new ParseException("Identifier '" + identifierToken.getValue() + "' already in use.");
                                }

                                return newDeclaration;
                            } 
                            else 
                            {
                                throw new ParseException("Expected ';', got " + peek().getValue(), peek());
                            }
                        } 
                        else 
                        {
                            throw new ParseException("Expected '=', got " + peek().getValue(), peek());
                        }
                    } 
                    else 
                    {
                        throw new ParseException("Expected identifier after 'int', got " + peek().getValue(), peek());
                    }
                }
                else
                {
                    throw new ParseException("Expected data type, got " + peek().getValue(), peek());
                }
            } 
            else 
            {
                throw new ParseException("Unexpected EOF");
            }
        } 
        catch (ParseException exception) 
        {
            throw exception;
        }
     }

     private Reassignment parseReassignment() throws ParseException
     {
        Token identifier;
        VarInfo info;
        Reassignment newReassignment;

        if (peek() != null)
        {
            if (peek().getType() == TokenType.IDENTIFIER)
            {
                identifier = consume();

                if (peek().getType() == TokenType.EQUALS)
                {
                    consume();

                    info = tableStack.getVarInfo(identifier.getValue());

                    if (info != null)
                    {
                        newReassignment = new Reassignment(identifier, parseExpression(info.getType(), peek()));

                        if (peek().getType() == TokenType.SEMICOLON)
                        {
                            consume();
                            return newReassignment;
                        }
                        else
                        {
                            throw new ParseException("Expected ';', got " + peek().getValue(), peek());
                        }
                    }
                    else
                    {
                        throw new ParseException("Undeclared variable '" + identifier.getValue() + "'", identifier);
                    }
                }
                else
                {
                    throw new ParseException("Expected '=', got " + peek().getValue(), peek());
                }
            }
            else
            {
                throw new ParseException("Expected identifier, got " + peek().getValue(), peek());
            }
        }
        else
        {
            throw new ParseException("Unexpected EOF");
        }
     }

     private Expression parseExpression(String type, Token exprToken) throws ParseException
     {
        switch (type)
        {
            case Tokenizer.TYPE_INT:
            NumExpression intExpression = parseNumExpression();

            if (!intExpression.isFloat())
            {
                return intExpression;
            }
            else
            {
                throw new ParseException("Cannot convert float expression to int", exprToken);
            }

            case Tokenizer.TYPE_FLOAT:
            return parseNumExpression();
            
            case Tokenizer.TYPE_CHAR:
            NumExpression charExpression = parseNumExpression();

            if (!charExpression.isFloat())
            {
                return charExpression;
            }
            else
            {
                throw new ParseException("Cannot convert float expression to char", exprToken);
            }

            case Tokenizer.TYPE_STRING:
            return parseStringExpression();

            default:
            throw new ParseException("Could not resolve expression", exprToken);
        }
     }

     private FunctionDeclaration parseFunctionDeclaration() throws ParseException
     {
        Token functionName;
        Token returnType;
        FunctionDeclaration newDeclaration;

        if (peek() != null)
        {
            if (peek().getType() == TokenType.TYPE)
            {
                returnType = consume();

                if (peek().getType() == TokenType.IDENTIFIER)
                {
                    functionName = consume();

                    if (peek().getType() == TokenType.OPEN_PAREN)
                    {
                        // consume open paren and create new function declaration statement
                        consume();
                        newDeclaration = new FunctionDeclaration(functionName.getValue(), returnType.getValue());

                        // add parameters to the declaration
                        parseParameters(newDeclaration);

                        // consume close paren
                        consume();

                        // if there is a semicolon next, leave the scope null and return the new declaration
                        if (peek().getType() == TokenType.SEMICOLON)
                        {
                            // consume semicolon

                            consume();

                            // make sure function isn't already declared
                            if (fTableStack.getFunctionDeclaration(functionName.getValue()) != null)
                            {
                                throw new ParseException("Redeclaration of the function '" + functionName.getValue() + "'", returnType);
                            }

                            // add function to table and return declaration statement
                            fTableStack.peek().put(functionName.getValue(), newDeclaration);
                            return newDeclaration;
                        }

                        // otherwise, if there is an open curly brace, parse the scope
                        if (peek().getType() == TokenType.OPEN_BRACE)
                        {
                            FunctionDeclaration existingDeclaration = fTableStack.getFunctionDeclaration(functionName.getValue());

                            // check if the function is already declared and there's a scope for it, in this case, there's an error
                            if (existingDeclaration != null && existingDeclaration.getScope() != null)
                            {
                                throw new ParseException("Attempt to redefine function '" + functionName.getValue() + "'", returnType);
                            }

                            // if the definitions are inconsistent, there's an error
                            if (existingDeclaration != null && !newDeclaration.equals(existingDeclaration))
                            {
                                throw new ParseException("Inconsistent prototype and implementation for function '" + functionName.getValue() + "'", returnType);
                            }

                            // set a placeholder scope so that if the function is redefined in the function body, it is not allowed
                            newDeclaration.setScope(new Scope(functionName.getValue()));

                            // put in the table with the placeholder scope
                            fTableStack.peek().put(functionName.getValue(), newDeclaration);

                            // get the actual scope for the function
                            newDeclaration.setScope(parseScope(functionName.getValue()));

                            // update table with the actual parsed scope
                            fTableStack.peek().put(functionName.getValue(), newDeclaration);
                            
                            return newDeclaration;
                        }

                        // if none of those are true, there's an error
                        throw new ParseException("Expected ';' or '{', got " + peek().getValue(), peek());
                    }
                    else
                    {
                        throw new ParseException("Expected ')', got " + peek().getValue(), peek());
                    }
                }
                else
                {
                    throw new ParseException("Expected identifier, got " + peek().getValue(), peek());
                }
            }
            else
            {
                throw new ParseException("Expected type, got " + peek().getValue(), peek());
            }
        }
        else
        {
            throw new ParseException("Unexpected EOF");
        }
     } 

     // parses parameters and adds them to an incomplete FunctionDeclaration
     private void parseParameters(FunctionDeclaration incompleteDeclaration) throws ParseException
     {
        String type;
        String identifier;

        while (peek() != null && peek().getType() != TokenType.CLOSE_PAREN)
        {
            if (peek().getType() == TokenType.TYPE)
            {
                type = consume().getValue();
            }
            else
            {
                throw new ParseException("Expected type, got " + peek().getValue(), peek()); 
            }

            if (peek().getType() == TokenType.IDENTIFIER)
            {
                identifier = consume().getValue();
            }
            else
            {
                throw new ParseException("Expected identifier, got " + peek().getValue(), peek());
            }

            incompleteDeclaration.addParam(new Parameter(type, identifier));

            // if the next token is not a close paren, check for a comma and consume it, otherwise there's an error
            if (peek().getType() != TokenType.CLOSE_PAREN)
            {
                if (peek().getType() == TokenType.COMMA)
                {
                    consume();
                }
                else
                {
                    throw new ParseException("Expected ',' or ')', instead got " + peek().getValue(), peek());
                }
            }
        }

        if (peek() == null)
        {
            throw new ParseException("Unexpected EOF");
        }
     }

     private ReturnStatement parseReturnStatement(String functionName) throws ParseException
     {
        FunctionDeclaration function;
        ReturnStatement newStmt;

        if (peek() != null)
        {
            if (peek().getType() == TokenType.RETURN)
            {
                if (functionName == null)
                {
                    throw new ParseException("Attempt to return outside of a function", peek());
                }

                function = fTableStack.getFunctionDeclaration(functionName);

                // I dont know how this would be possible but it's there anyway 
                if (function == null)
                {
                    throw new ParseException("Attempt to return in function that doesn't exist?", peek());
                }

                if (function.getReturnType().equals(Tokenizer.TYPE_VOID))
                {
                    throw new ParseException("Attempt to return in a void function", peek());
                }

                consume();

                newStmt = new ReturnStatement(parseExpression(function.getReturnType(), peek()));

                if (peek().getType() == TokenType.SEMICOLON)
                {
                    consume();
                    return newStmt;
                }
                else
                {
                    throw new ParseException("Expected ';', got " + peek().getValue(), peek());
                }
            }
            else
            {
                throw new ParseException("Expected 'return', got " + peek().getValue(), peek());
            }
        }
        else
        {
            throw new ParseException("Unexpected EOF");
        }
     }

     private PrintStatement parsePrintStatement() throws ParseException
     {
        StringExpression expression;

        if (peek() != null && peek().getType() == TokenType.PRINT)
        {
            consume();

            expression = parseStringExpression();

            if ( peek() != null && peek().getType() == TokenType.SEMICOLON )
            {
                consume();
                return new PrintStatement(expression);
            }

            throw new ParseException("Expected ';', got " + peek().getValue(), peek());
        }

        throw new ParseException("Expected 'print', got " + peek().getValue(), peek());
     }

     private ExitStatement parseExitStatement() throws ParseException
     {
        NumExpression expression;

        if (peek() != null && peek().getType() == TokenType.EXIT)
        {
            consume();

            expression = parseNumExpression();

            if (expression.isFloat())
            {
                throw new ParseException("Exit code cannot be a float value");
            }

            if ( peek() != null && peek().getType() == TokenType.SEMICOLON )
            {
                consume();
                return new ExitStatement(expression);
            }

            throw new ParseException("Expected ';', got " + peek().getValue(), peek());
        }

        throw new ParseException("Expected 'exit', got " + peek().getValue(), peek());
     }

     private NumExpression parseNumExpression() throws ParseException
     {
        NumExpression expression;
        NumExpression rightExpression;
        NumTerm term;
        Token operator;
        
        term = parseNumTerm();
        expression = new NumExpression(term);
        
        while ( peek() != null &&
            peek().getType() == TokenType.PLUS || peek().getType() == TokenType.MINUS)
        {
            operator = consume();
            term = parseNumTerm();
            rightExpression = new NumExpression(term);
            expression = new NumExpression(expression, operator, rightExpression);
        }

        return expression;
     }

     private NumTerm parseNumTerm() throws ParseException
     {
        NumFactor factor;
        NumTerm term;
        Token operator;
        NumTerm right;

        factor = parseNumFactor();
        term = new NumTerm(factor);

        while (peek() != null &&
                (peek().getType() == TokenType.TIMES || 
                peek().getType() == TokenType.DIVISION ||
                peek().getType() == TokenType.MOD))
        {
            operator = consume();
            factor = parseNumFactor();
            right = new NumTerm(factor);

            term = new NumTerm(term, operator, right);
        }

        return term;
     }

     private NumFactor parseNumFactor() throws ParseException
     {
        NumFactor newFactor;
        boolean negative = false;

        if (peek() != null)
        {
            if (peek().getType() == TokenType.MINUS)
            {
                negative = true;
                consume();
            }

            if (peek().getType() == TokenType.LITERAL_INT ||
                peek().getType() == TokenType.LITERAL_FLOAT ||
                peek().getType() == TokenType.IDENTIFIER ||
                peek().getType() == TokenType.LITERAL_CHAR)
            {
                newFactor = new NumFactor(consume(), negative);

                switch (newFactor.getToken().getType())
                {
                    case LITERAL_FLOAT:
                    litTable.addLiteralWithType(newFactor.getToken().getValue(), Tokenizer.TYPE_FLOAT);
                    newFactor.setFloat(true);
                    break;

                    case IDENTIFIER:
                    String identifierStr = newFactor.getToken().getValue();

                    if (tableStack.identifierInUse(identifierStr))
                    {
                        newFactor.setFloat(tableStack.getVarInfo(identifierStr).getType().equals(Tokenizer.TYPE_FLOAT));
                        break;
                    }
                    else
                    {
                        throw new ParseException("Unknown identifier: " + identifierStr, newFactor.getToken());
                    }

                    default:
                    newFactor.setFloat(false);
                    break;
                }

                return newFactor;
            }
            else if (peek().getType() == TokenType.OPEN_PAREN)
            {
                consume();
                newFactor = new NumFactor(parseNumExpression(), negative);

                if (peek().getType() == TokenType.CLOSE_PAREN)
                {
                        consume();
                    return newFactor;
                }
                        
                throw new ParseException("Expected ')', got " + peek().getValue(), peek());
            }

            throw new ParseException("Expected literal, identifier, or '(', got " + peek().getValue(), peek());
        }

        throw new ParseException("Expected int factor, got EOF");
     }

     private StringExpression parseStringExpression() throws ParseException
     {
        if (peek() != null)
        {
            if (peek().getType() == TokenType.LITERAL_STR)
            {
                Token temp = consume();
                litTable.addLiteralWithType(temp.getValue(), Tokenizer.TYPE_STRING);
                return new StringExpression(temp);
            }
            else if (peek().getType() == TokenType.IDENTIFIER)
            {
                return new StringExpression(consume());
            }

            throw new ParseException("Expected string literal or identifier, got " + peek().getValue(), peek());
        }

        throw new ParseException("Expected string literal or identifier, got EOF");
     }

     private Token peek()
     {
        if (tokenPos < tokenList.size())
        {
            return tokenList.get(tokenPos);
        }

        return null;
     }

     private Token peek(int ahead)
     {
        if (tokenPos + ahead < tokenList.size())
        {
            return tokenList.get(tokenPos + ahead);
        }

        return null;
     }

     private Token consume()
     {
        if (tokenPos < tokenList.size())
        {
            Token returnToken = tokenList.get(tokenPos);
            tokenPos++;
            return returnToken;
        }

        return null;
     }
}
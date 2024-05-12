package parser;

import java.util.List;

import codegen.SymbolTable;
import codegen.VarInfo;

import java.util.ArrayList;

import tokenizer.Token;
import tokenizer.TokenType;
import tokenizer.Tokenizer;

public class Parser 
{
     private List<Statement> program = new ArrayList<Statement>();
     private List<Token> tokenList = new ArrayList<Token>();
     private SymbolTable symbolTable = new SymbolTable();
     private LiteralTable litTable = new LiteralTable();
     private int tokenPos;

     public Parser(List<Token> tokens)
     {
        tokenList = tokens;
     }

     public boolean parseProgram() throws ParseException
     {
        program.clear();
        tokenPos = 0;
        boolean exit = false;

        while (!exit && tokenPos < tokenList.size())
        {
            program.add(parseStatement());
        }

        return !exit;
     }

     public void printStatements()
     {
        for (int i = 0; i < program.size(); i++)
        {
            Statement statement = program.get(i);
            statement.printStatement();
        }
     }

     public List<Statement> getProgram()
     {
        return program;
     }

     public SymbolTable getSymbolTable()
     {
        return symbolTable;
     }

     public LiteralTable getLitTable()
     {
        return litTable;
     }

     private Statement parseStatement() throws ParseException
     {
        try
        {
            if (peek() != null)
            {
                switch( peek().getType() )
                {
                    case TYPE:
                    return parseDeclaration();

                    case IDENTIFIER:
                    return parseReassignment();
                    
                    case PRINT:
                    return parsePrintStatement();

                    case EXIT:
                    return parseExitStatement();

                    default:
                    throw new ParseException("Expected 'int' or 'exit', instead got " + 
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

                            switch (typeToken.getValue())
                            {
                                case Tokenizer.TYPE_INT:
                                NumExpression intExpression = parseNumExpression();

                                if (!intExpression.isFloat())
                                {
                                    newDeclaration = new NumDeclaration(typeToken, identifierToken, intExpression);
                                    break;
                                }
                                else
                                {
                                    throw new ParseException("Cannot convert float expression to int", typeToken);
                                }

                                case Tokenizer.TYPE_FLOAT:
                                NumExpression floatExpression = parseNumExpression();
                                newDeclaration = new NumDeclaration(typeToken, identifierToken, floatExpression);
                                break;

                                case Tokenizer.TYPE_CHAR:
                                NumExpression charExpression = parseNumExpression();

                                if (!charExpression.isFloat())
                                {
                                    newDeclaration = new CharDeclaration(typeToken, identifierToken, charExpression);
                                    break;
                                }
                                else
                                {
                                    throw new ParseException("Cannot convert float expression to char", typeToken);
                                }

                                case Tokenizer.TYPE_STRING:
                                StringExpression strExpression = parseStringExpression();
                                newDeclaration = new StringDeclaration(typeToken, identifierToken, strExpression);
                                break;

                                default:
                                throw new ParseException("Unknown data type: " + typeToken.getValue(), typeToken);
                            }
                            
                            if ( peek() != null && peek().getType() == TokenType.SEMICOLON ) 
                            {
                                consume();
                                
                                if (!symbolTable.addIdentifier(typeToken.getValue(), identifierToken.getValue()))
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

                    info = symbolTable.getVarInfo(identifier.getValue());

                    if (info != null)
                    {
                        switch (info.getType())
                        {
                            case Tokenizer.TYPE_INT:
                            NumExpression intExpression = parseNumExpression();

                            if (!intExpression.isFloat())
                            {
                                newReassignment = new NumReassignment(identifier, intExpression);
                                break;
                            }
                            else
                            {
                                throw new ParseException("Cannot convert float expression to int", identifier);
                            }

                            case Tokenizer.TYPE_FLOAT:
                            NumExpression floatExpression = parseNumExpression();
                            newReassignment = new NumReassignment(identifier, floatExpression);
                            break;

                            case Tokenizer.TYPE_CHAR:
                            NumExpression charExpression = parseNumExpression();

                            if (!charExpression.isFloat())
                            {
                                newReassignment = new NumReassignment(identifier, charExpression);
                                break;
                            }
                            else
                            {
                                throw new ParseException("Cannot convert float expression to char", identifier);
                            }

                            case Tokenizer.TYPE_STRING:
                            StringExpression strExpression = parseStringExpression();
                            newReassignment = new StringReassignment(identifier, strExpression);
                            break;

                            default:
                            throw new ParseException("Unknown type of identifier '" + identifier.getValue() + "': " + info.getType(), identifier);
                        }

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

                    if (symbolTable.identifierExists(identifierStr))
                    {
                        newFactor.setFloat(symbolTable.getIdentifierType(identifierStr).equals(Tokenizer.TYPE_FLOAT));
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
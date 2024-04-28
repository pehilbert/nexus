package parser;

import java.util.List;
import java.util.ArrayList;

import tokenizer.Token;
import tokenizer.TokenType;
import tokenizer.Tokenizer;

public class Parser 
{
     private List<Statement> program = new ArrayList<Statement>();
     private List<Token> tokenList = new ArrayList<Token>();
     private SymbolTable symbolTable = new SymbolTable();
     private int tokenPos;

     public static final int INT_SIZE = 4;
     public static final int CHAR_SIZE = 1;
     public static final int PTR_SIZE = 4;

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
                            int newVarSize;
                            int ptrDataSize = -1;
                            boolean pointer = false;

                            switch (typeToken.getValue())
                            {
                                case Tokenizer.TYPE_INT:
                                IntExpression intExpression = parseIntExpression();
                                newDeclaration = new IntDeclaration(typeToken, identifierToken, intExpression);
                                newVarSize = INT_SIZE;
                                break;

                                case Tokenizer.TYPE_CHAR:
                                IntExpression charExpression = parseIntExpression();
                                newDeclaration = new CharDeclaration(typeToken, identifierToken, charExpression);
                                newVarSize = CHAR_SIZE;
                                break;

                                case Tokenizer.TYPE_STRING:
                                StringExpression strExpression = parseStringExpression();
                                newDeclaration = new StringDeclaration(typeToken, identifierToken, strExpression);

                                pointer = true;
                                newVarSize = PTR_SIZE;
                                ptrDataSize = getStringSize(strExpression);
                                break;

                                default:
                                throw new ParseException("Unknown data type: " + typeToken.getValue(), typeToken);
                            }
                            
                            if ( peek() != null && peek().getType() == TokenType.SEMICOLON ) 
                            {
                                consume();
                                
                                if (!symbolTable.addIdentifier(typeToken.getValue(), identifierToken.getValue(), newVarSize))
                                {
                                    throw new ParseException("Identifier '" + identifierToken.getValue() + "' already in use.");
                                }

                                if (pointer)
                                {
                                    if (ptrDataSize != -1)
                                    {
                                        switch (typeToken.getValue())
                                        {
                                            case Tokenizer.TYPE_STRING:
                                            symbolTable.makePointer(identifierToken.getValue(), Tokenizer.TYPE_CHAR, 
                                                                                                CHAR_SIZE, ptrDataSize);
                                            break;

                                            default:
                                            throw new ParseException("Unsupported pointer type: " + typeToken.getValue(), typeToken);
                                        }
                                    }
                                    else
                                    {
                                        throw new ParseException("Could not resolve the data size of the pointer " + identifierToken.getValue(), identifierToken);
                                    }
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

        try
        {
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
                                IntExpression intExpression = parseIntExpression();
                                newReassignment = new IntReassignment(identifier, intExpression);
                                break;

                                case Tokenizer.TYPE_CHAR:
                                IntExpression charExpression = parseIntExpression();
                                newReassignment = new IntReassignment(identifier, charExpression);
                                break;

                                case Tokenizer.TYPE_STRING:
                                StringExpression strExpression = parseStringExpression();
                                newReassignment = new StringReassignment(identifier, strExpression);
                                break;

                                default:
                                throw new ParseException("Unknown type of identifier '" + identifier.getValue() + "'", identifier);
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
        catch (ParseException exception)
        {
            throw exception;
        }
     }

     private int getStringSize(StringExpression strExpression) throws ParseException
     {
        switch (strExpression.getToken().getType())
        {
            case LITERAL_STR:
            return strExpression.getToken().getValue().length() + 1;

            case IDENTIFIER:
            VarInfo otherVarInfo = symbolTable.getVarInfo(strExpression.getToken().getValue());

            if (otherVarInfo != null && otherVarInfo.getType().equals(Tokenizer.TYPE_STRING))
            {
                return otherVarInfo.getTotalSize();
            }

            throw new ParseException("Expected identifier of type str, got one of type " + otherVarInfo.getType(), strExpression.getToken());

            default:
            throw new ParseException("Invalid token " + strExpression.getToken().getValue() + " at this position.");
        }
     }

     private ExitStatement parseExitStatement() throws ParseException
     {
        IntExpression expression;

        try
        {
            if (peek() != null && peek().getType() == TokenType.EXIT)
            {
                consume();

                expression = parseIntExpression();

                if ( peek() != null && peek().getType() == TokenType.SEMICOLON )
                {
                    consume();
                    return new ExitStatement(expression);
                }

                throw new ParseException("Expected ';', got " + peek().getValue(), peek());
            }

            throw new ParseException("Expected 'exit', got " + peek().getValue(), peek());
        }
        catch (ParseException exception)
        {
            throw exception;
        }
     }

     private IntExpression parseIntExpression() throws ParseException
     {
        IntExpression expression;
        IntExpression rightExpression;
        IntTerm term;
        Token operator;
        
        try
        {
            term = parseIntTerm();
            expression = new IntExpression(term);
            
            while ( peek() != null &&
                peek().getType() == TokenType.PLUS || peek().getType() == TokenType.MINUS)
            {
                operator = consume();
                term = parseIntTerm();
                rightExpression = new IntExpression(term);
                expression = new IntExpression(expression, operator, rightExpression);
            }
        }
        catch (ParseException exception)
        {
            throw exception;
        }

        return expression;
     }

     private IntTerm parseIntTerm() throws ParseException
     {
        IntFactor factor;
        IntTerm term;
        Token operator;
        IntTerm right;

        try
        {
            factor = parseIntFactor();
            term = new IntTerm(factor);

            while (peek() != null &&
                    (peek().getType() == TokenType.TIMES || 
                    peek().getType() == TokenType.DIVISION ||
                    peek().getType() == TokenType.MOD))
            {
                operator = consume();
                factor = parseIntFactor();
                right = new IntTerm(factor);

                term = new IntTerm(term, operator, right);
            }
        }
        catch (ParseException exception)
        {
            throw exception;
        }

        return term;
     }

     private IntFactor parseIntFactor() throws ParseException
     {
        IntFactor newFactor;
        boolean negative = false;

        try
        {
            if (peek() != null)
            {
                if (peek().getType() == TokenType.MINUS)
                {
                    negative = true;
                    consume();
                }

                if (peek().getType() == TokenType.LITERAL_INT ||
                    peek().getType() == TokenType.IDENTIFIER ||
                    peek().getType() == TokenType.LITERAL_CHAR)
                {
                    return new IntFactor(consume(), negative);
                }
                else if (peek().getType() == TokenType.OPEN_PAREN)
                {
                    consume();
                    newFactor = new IntFactor(parseIntExpression(), negative);

                    if (peek().getType() == TokenType.CLOSE_PAREN)
                    {
                           consume();
                        return newFactor;
                    }
                            
                    throw new ParseException("Expected ')', got " + peek().getValue(), peek());
                }

                throw new ParseException("Expected int literal, identifier, or '(', got " + peek().getValue(), peek());
            }

            throw new ParseException("Expected int factor, got EOF");
        }
        catch (ParseException exception)
        {
            throw exception;
        }
     }

     private StringExpression parseStringExpression() throws ParseException
     {
        if (peek() != null)
        {
            if (peek().getType() == TokenType.LITERAL_STR || peek().getType() == TokenType.IDENTIFIER)
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
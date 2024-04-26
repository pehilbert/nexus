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
                                IntExpression expression = parseIntExpression();
                                newDeclaration = new IntDeclaration(typeToken, identifierToken, expression);
                                System.out.println("Statement parsed: ");
                                System.out.println(newDeclaration.getType().getValue());
                                System.out.println(newDeclaration.getIdentifier().getValue());
                                break;

                                default:
                                throw new ParseException("Unknown data type: " + typeToken.getValue(), typeToken);
                            }
                            
                            if ( peek() != null && peek().getType() == TokenType.SEMICOLON ) 
                            {
                                consume();
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
                throw new ParseException("Expected 'int', got " + peek().getValue(), peek());
            }
        } 
        catch (ParseException exception) 
        {
            throw exception;
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
                peek().getType() == TokenType.IDENTIFIER)
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
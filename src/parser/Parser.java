package parser;

import java.util.List;
import java.util.ArrayList;

import tokenizer.Token;
import tokenizer.TokenType;

public class Parser 
{
     private List<Statement> program = new ArrayList<Statement>();
     private List<Token> tokenList = new ArrayList<Token>();
     private int tokenPos;

    public Parser(List<Token> tokens)
    {
        tokenList = tokens;
    }

     public void parseProgram()
     {
        program.clear();
        tokenPos = 0;
        boolean exit = false;

        while (!exit && tokenPos < tokenList.size())
        {
            try
            {
                program.add( parseStatement() );

            }
            catch (ParseException exception)
            {
                exception.printStackTrace();
                exit = true;
            }
        }
     }

     public void printStatements()
     {
        for (int i = 0; i < program.size(); i++)
        {
            Statement statement = program.get(i);
            statement.printStatement();
        }
     }

     private Statement parseStatement() throws ParseException
     {
        if (peek() != null)
        {
            switch( peek().getType() )
            {
                case TokenType.TYPE_INT:
                return parseIntDeclaration();
                
                case TokenType.EXIT:
                return parseExitStatement();

                default:
                throw new ParseException("Expected 'int' or 'exit', instead got " + 
                                          peek().getValue());
            }
        }

        return null;
     }

     private IntDeclaration parseIntDeclaration() throws ParseException
     {
        Token identifierToken;
        IntExpression expression;

        if (peek() != null && consume().getType() == TokenType.TYPE_INT 
            && peek().getType() == TokenType.IDENTIFIER)
        {
            identifierToken = consume();

            if (peek() != null && peek().getType() == TokenType.EQUALS)
            {
                consume();
                
                try
                {
                    expression = parseIntExpression();
                    
                    if ( peek() != null && peek().getType() == TokenType.SEMICOLON )
                    {
                        consume();
                        return new IntDeclaration(identifierToken, expression);
                    }

                    throw new ParseException("Expected ';', got " + peek().getValue());
                }
                catch (ParseException exception)
                {
                    exception.printStackTrace();
                }
            }

            throw new ParseException("Expected '=', got " + peek().getValue());
        }
        
        throw new ParseException("Expected 'int', got " + peek().getValue());
     }

     private ExitStatement parseExitStatement() throws ParseException
     {
        IntExpression expression;

        if (peek() != null && peek().getType() == TokenType.EXIT)
        {
            consume();

            try
            {
                expression = parseIntExpression();

                if ( peek() != null && peek().getType() == TokenType.SEMICOLON )
                {
                    consume();
                    return new ExitStatement(expression);
                }

                throw new ParseException("Expected ';', got " + peek().getValue());
            }
            catch (ParseException exception)
            {
                exception.printStackTrace();
            }
        }

        throw new ParseException("Expected 'exit', got " + peek().getValue());
     }

     private IntExpression parseIntExpression() throws ParseException
     {
        IntTerm term;

        if ( peek() != null )
        {
            try
            {
                term = parseIntTerm();
                return new IntExpression(term);
            }
            catch (ParseException exception)
            {
                exception.printStackTrace();
            }
        }

        throw new ParseException("Expected int term, got nothing");
     }

     private IntTerm parseIntTerm() throws ParseException
     {
        if (peek() != null && 
            (peek().getType() == TokenType.IDENTIFIER || 
             peek().getType() == TokenType.LITERAL_INT) )
        {
            return new IntTerm( consume() );
        }

        throw new ParseException("Expected identifier or int literal, got " + 
                                 peek().getValue());
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

interface Statement 
{
    public void printStatement();
}

class IntDeclaration implements Statement 
{
    private Token identifierToken;
    private IntExpression expression;

    public IntDeclaration(Token identifier, IntExpression expr)
    {
        identifierToken = identifier;
        expression = expr;
    }

    public void printStatement()
    {
        System.out.println("Int declaration: " + identifierToken.getValue() + 
                            ", " + expression.getTerm().getToken().getValue());
    }

    public Token getIdentifier()
    {
        return identifierToken;
    }

    public IntExpression getExpression()
    {
        return expression;
    }
}

class ExitStatement implements Statement 
{
    IntExpression expression;

    public ExitStatement(IntExpression expr)
    {
        expression = expr;
    }

    public void printStatement()
    {
        System.out.println("Exit statement: " + 
                            expression.getTerm().getToken().getValue());
    }

    public IntExpression getExpression()
    {
        return expression;
    }
}

class IntExpression
{
    private IntTerm term;

    public IntExpression(IntTerm inTerm)
    {
        term = inTerm;
    }

    public IntTerm getTerm()
    {
        return term;
    }
}

class IntTerm
{
    private Token token;

    public IntTerm(Token inToken)
    {
        token = inToken;
    }

    public Token getToken()
    {
        return token;
    }
}
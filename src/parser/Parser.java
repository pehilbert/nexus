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

     public List<Statement> getProgram()
     {
        return program;
     }

     private Statement parseStatement() throws ParseException
     {
        if (peek() != null)
        {
            switch( peek().getType() )
            {
                case TYPE_INT:
                return parseIntDeclaration();
                
                case EXIT:
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
        Token operator;
        IntExpression expression;

        try
        {
            term = parseIntTerm();

            if (peek() != null &&
                (peek().getType() == TokenType.PLUS || peek().getType() == TokenType.MINUS))
            {
                operator = consume();
                expression = parseIntExpression();
                return new IntExpression(expression, operator, term);
            }

            return new IntExpression(term);
        }
        catch (ParseException exception)
        {
            exception.printStackTrace();
        }

        throw new ParseException("Could not parse int expression");
     }

     private IntTerm parseIntTerm() throws ParseException
     {
        IntFactor factor;
        Token operator;
        IntTerm term;

        try
        {
            factor = parseIntFactor();

            if (peek() != null &&
                (peek().getType() == TokenType.TIMES || peek().getType() == TokenType.DIVISION))
            {
                operator = consume();
                term = parseIntTerm();
                return new IntTerm(term, operator, factor);
            }

            return new IntTerm(factor);
        }
        catch (ParseException exception)
        {
            exception.printStackTrace();
        }

        throw new ParseException("Could not parse int term");
     }

     private IntFactor parseIntFactor() throws ParseException
     {
        IntFactor newFactor;

        if (peek() != null)
        {
            if (peek().getType() == TokenType.LITERAL_INT ||
            peek().getType() == TokenType.IDENTIFIER)
            {
                return new IntFactor(consume());
            }
            else if (peek().getType() == TokenType.OPEN_PAREN)
            {
                consume();
                
                try
                {
                    newFactor = new IntFactor(parseIntExpression());

                    if (peek().getType() == TokenType.CLOSE_PAREN)
                    {
                        consume();
                        return newFactor;
                    }
                        
                    throw new ParseException("Expected ')', got " + peek().getValue());
                }
                catch (ParseException exception)
                {
                    exception.printStackTrace();
                }
            }

            throw new ParseException("Expected int literal, identifier, or '(', got " + peek().getValue());
        }

        throw new ParseException("Expected int factor, got nothing");
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
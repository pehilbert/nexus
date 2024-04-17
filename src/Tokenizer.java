import java.util.List;
import java.util.ArrayList;

public class Tokenizer {
    private List<Token> tokenList = new ArrayList<Token>();
    private String str;
    private int strPos;

    static final String NULL_STR = "\0";
    static final char UNDERSCORE = '_';

    static final String EQUALS = "=";
    static final String SEMICOLON = ";";
    static final String EXIT = "exit";
    static final String TYPE_INT = "int";

    public Tokenizer(String inStr)
    {
        this.str = inStr;
    }

    public void tokenize() throws TokenException
    {
        tokenList.clear();
        strPos = 0;
        
        String buffer = "";
        
        while (peek() != NULL_STR)
        {

            if (isStringWhitespace( peek() ))
            {
                if (buffer.length() > 0)
                {
                    tokenList.add( getTokenFromString(buffer) );
                    buffer = "";
                }

                consume();
            }
            else if (buffer.length() > 0)
            {
                switch( peek() )
                {
                    case EQUALS:
                    tokenList.add( getTokenFromString(buffer) );
                    tokenList.add( getTokenFromString(consume()) );
                    buffer = "";
                    break;

                    case SEMICOLON:
                    tokenList.add( getTokenFromString(buffer) );
                    tokenList.add( getTokenFromString(consume()) );
                    buffer = "";
                    break;

                    default:
                    buffer += consume();
                }
            }
            else
            {
                buffer += consume();
            }
        }
    }

    public void printTokenList()
    {
        for (int i = 0; i < tokenList.size(); i++)
        {
            Token token = tokenList.get(i);
            System.out.println(token.getType() + ", " + token.getValue() + ", " + token.getPos());
        }
    }

    private String peek()
    {
        if (strPos < str.length())
        {
            return "" + str.charAt(strPos);
        }

        return NULL_STR;
    }

    private String consume()
    {
        if (strPos < str.length())
        {
            String returnChar = "" + str.charAt(strPos);
            strPos++;
            return returnChar;
        }

        return NULL_STR;
    }

    private Token getTokenFromString(String test) throws TokenException
    {
        switch (test)
        {
            case EQUALS:
            return new Token(TokenType.EQUALS, test, strPos - test.length() + 1);

            case SEMICOLON:
            return new Token(TokenType.SEMICOLON, test, strPos - test.length() + 1);

            case EXIT:
            return new Token(TokenType.EXIT, test, strPos - test.length() + 1);

            case TYPE_INT:
            return new Token(TokenType.TYPE_INT, test, strPos - test.length() + 1);

            default:
            if (isIdentifier(test))
            {
                return new Token(TokenType.IDENTIFIER, test, strPos - test.length() + 1);
            }

            if (isIntLiteral(test))
            {
                return new Token(TokenType.LITERAL_INT, test, strPos - test.length() + 1);
            }

            throw new TokenException("You messed up!");
        }
    }

    private boolean isIdentifier(String test)
    {
        for (int i = 0; i < test.length(); i++)
        {
            if (!(Character.isAlphabetic(test.charAt(i)) || test.charAt(i) == UNDERSCORE))
            {
                return false;
            }
        }

        return true;
    }

    private boolean isIntLiteral(String test)
    {
        for (int i = 0; i < test.length(); i++)
        {
            if (!Character.isDigit(test.charAt(i)))
            {
                return false;
            }
        }

        return true;
    }

    private boolean isStringWhitespace(String test)
    {
        for (int i = 0; i < test.length(); i++)
        {
            if (!Character.isWhitespace(test.charAt(i)))
            {
                return false;
            }
        }

        return true;
    }
}

enum TokenType 
{
    TYPE_INT, 
    LITERAL_INT,

    IDENTIFIER, 
    EXIT,

    EQUALS,
    SEMICOLON,
}

class TokenException extends Exception
{
    public TokenException(String message)
    {
        super(message);
    }
}

class Token 
{
    private TokenType type;
    private String value;
    private int pos;

    public Token(TokenType type, String value, int pos) 
    {
        this.type = type;
        this.value = value;
        this.pos = pos;
    }

    // Getter methods
    public TokenType getType() 
    {
        return type;
    }

    public String getValue() 
    {
        return value;
    }

    public int getPos() 
    {
        return pos;
    }
}
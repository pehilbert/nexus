package tokenizer;

import java.util.List;
import java.util.ArrayList;

public class Tokenizer {
    private List<Token> tokenList = new ArrayList<Token>();
    private String str;
    private int strPos;

    static final String NULL_STR = "\0";
    static final char UNDERSCORE = '_';

    static final String PLUS = "+";
    static final String MINUS = "-";
    static final String TIMES = "*";
    static final String DIVISION = "/";
    static final String EQUALS = "=";
    static final String OPEN_PAREN = "(";
    static final String CLOSE_PAREN = ")";
    static final String HASHTAG = "#";
    static final String OPEN_BRACKET = "[";
    static final String CLOSE_BRACKET = "]";
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
            if (isStringWhitespace( peek() ) || peek() == HASHTAG)
            {
                if (buffer.length() > 0)
                {
                    tokenList.add( getTokenFromString(buffer) );
                    buffer = "";
                }
                
                if (consume() == HASHTAG)
                {
                    if (consume() == OPEN_BRACKET)
                    {
                        boolean commentClosed = false;

                        while (!commentClosed)
                        {
                            String next = consume();

                            if (next == NULL_STR)
                            {
                                throw new TokenException("Expected ']#' to close comment");
                            }

                            if (next == CLOSE_BRACKET && peek() == HASHTAG)
                            {
                                consume();
                                commentClosed = true;
                            }
                        }
                    }
                }
            }
            else if (buffer.length() > 0)
            {
                switch( peek() )
                {
                    case PLUS:
                    case MINUS:
                    case TIMES:
                    case DIVISION:
                    case OPEN_PAREN:
                    case CLOSE_PAREN:
                    case EQUALS:
                    case SEMICOLON:
                    tokenList.add( getTokenFromString(buffer) );
                    tokenList.add( getTokenFromString( consume() ) );
                    buffer = "";

                    case HASHTAG:
                    break;

                    default:
                    switch (buffer)
                    {
                        case PLUS:
                        case MINUS:
                        case TIMES:
                        case DIVISION:
                        case OPEN_PAREN:
                        case CLOSE_PAREN:
                        case EQUALS:
                        case SEMICOLON:
                        tokenList.add( getTokenFromString(buffer) );
                        buffer = "";
                        break;

                        case HASHTAG:
                        break;

                        default:
                        buffer += consume();
                    }
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
    
    public List<Token> getTokens()
    {
        return tokenList;
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
            case PLUS:
            return new Token(TokenType.PLUS, test, strPos - test.length() + 1);

            case MINUS:
            return new Token(TokenType.MINUS, test, strPos - test.length() + 1);

            case TIMES:
            return new Token(TokenType.TIMES, test, strPos - test.length() + 1);

            case DIVISION:
            return new Token(TokenType.DIVISION, test, strPos - test.length() + 1);

            case OPEN_PAREN:
            return new Token(TokenType.OPEN_PAREN, test, strPos - test.length() + 1);

            case CLOSE_PAREN:
            return new Token(TokenType.CLOSE_PAREN, test, strPos - test.length() + 1);

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

            throw new TokenException("'" + test + "' is not a valid token.");
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
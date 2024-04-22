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
    static final String ENDLINE = "\n";
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
            switch (peek())
            {
                // single-character tokens that would end any current token
                case PLUS:
                case MINUS:
                case TIMES:
                case DIVISION:
                case EQUALS:
                case OPEN_PAREN:
                case CLOSE_PAREN:
                case SEMICOLON:
                // if something was already read, tokenize it
                if (buffer.length() > 0)
                {
                    tokenList.add(getTokenFromString(buffer));
                    buffer = "";
                }

                // also tokenize the current single-character token
                tokenList.add(getTokenFromString(consume()));
                break;

                // handle comments
                case HASHTAG:
                // if something was already read, tokenize it
                if (buffer.length() > 0)
                {
                    tokenList.add(getTokenFromString(buffer));
                    buffer = "";
                }

                consume();

                System.out.println("Character after hashtag: " + peek());

                // handle multi-line comment
                if (peek() == OPEN_BRACKET)
                {
                    System.out.println("Multiline comment");
                    consume();
                    boolean endComment = false;

                    while (!endComment && peek() != NULL_STR)
                    {
                        if (consume() == CLOSE_BRACKET && consume() == HASHTAG)
                        {
                            endComment = true;
                        }
                    }

                    if (!endComment)
                    {
                        throw new TokenException("Could not find the end of the multiline comment");
                    }
                }
                // handle single-line comment
                else
                {
                    System.out.println("Single line comment");
                    while (peek() != NULL_STR && consume() != ENDLINE)
                    {
                        
                    }
                }

                break;

                // handle everything else
                default:

                // handle whitespace
                if (isStringWhitespace(peek()))
                {
                    // if something was already read, tokenize it
                    if (buffer.length() > 0)
                    {
                        tokenList.add(getTokenFromString(buffer));
                        buffer = "";
                    }
                    
                    // consume current whitespace character
                    consume();
                }
                // handle non-whitespace
                else 
                {
                    // just keep reading
                    buffer += consume();
                }
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
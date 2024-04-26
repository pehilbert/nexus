package tokenizer;

import java.util.List;
import java.util.ArrayList;

public class Tokenizer {
    private List<Token> tokenList = new ArrayList<Token>();
    private String str;
    private int strPos;
    private int currentLine;
    private int currentCol;

    static final char NULL_CHAR = '\0';
    static final char UNDERSCORE = '_';

    static final char PLUS = '+';
    static final char MINUS = '-';
    static final char TIMES = '*';
    static final char DIVISION = '/';
    static final char MOD = '%';
    static final char EQUALS = '=';
    static final char OPEN_PAREN = '(';
    static final char CLOSE_PAREN = ')';
    static final char HASHTAG = '#';
    static final char OPEN_BRACKET = '[';
    static final char CLOSE_BRACKET = ']';
    static final char SEMICOLON = ';';
    static final char ENDLINE = '\n';

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
        currentLine = 1;
        currentCol = 1;
        
        String buffer = "";
        
        while (peek() != NULL_CHAR)
        {
            switch (peek())
            {
                // single-character tokens that would end any current token
                case PLUS:
                case MINUS:
                case TIMES:
                case DIVISION:
                case MOD:
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
                tokenList.add(getTokenFromString("" + consume()));
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

                // handle multi-line comment
                if (peek() == OPEN_BRACKET)
                {
                    consume();
                    consumeMultiLineComment();
                }
                // handle single-line comment
                else
                {
                    while (peek() != NULL_CHAR && consume() != ENDLINE) {}
                }

                break;

                // handle everything else
                default:

                // handle whitespace
                if (Character.isWhitespace(peek()))
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

    // consumes characters until "]#" is found, but if another "#["
    // is found, recursively calls itself to consume that properly as well
    public void consumeMultiLineComment() throws TokenException
    {
        while (peek() != NULL_CHAR)
        {
            if (peek() == CLOSE_BRACKET)
            {
                consume();

                if (peek() == HASHTAG)
                {
                    consume();
                    return;
                }
            }
            else if (peek() == HASHTAG)
            {
                consume();

                if (peek() == OPEN_BRACKET)
                {
                    consume();
                    consumeMultiLineComment();
                }
            }
            else
            {
                consume();
            }
        }

        throw new TokenException("Could not find the end of multi-line comment");
    }

    public void printTokenList()
    {
        for (int i = 0; i < tokenList.size(); i++)
        {
            Token token = tokenList.get(i);
            System.out.println(token.toString());
        }
    }
    
    public List<Token> getTokens()
    {
        return tokenList;
    }

    private char peek()
    {
        if (strPos < str.length())
        {
            return str.charAt(strPos);
        }

        return NULL_CHAR;
    }

    private char consume()
    {
        if (strPos < str.length())
        {
            char returnChar = str.charAt(strPos);
            strPos++;

            if (returnChar == ENDLINE)
            {
                currentLine++;
                currentCol = 1;
            }
            else
            {
                currentCol++;
            }

            return returnChar;
        }

        return NULL_CHAR;
    }

    private Token getTokenFromString(String test) throws TokenException
    {
        // Test single character tokens
        switch (test.charAt(0))
        {
            case PLUS:
            return new Token(TokenType.PLUS, test, currentLine, currentCol);

            case MINUS:
            return new Token(TokenType.MINUS, test, currentLine, currentCol);

            case TIMES:
            return new Token(TokenType.TIMES, test, currentLine, currentCol);

            case DIVISION:
            return new Token(TokenType.DIVISION, test, currentLine, currentCol);

            case MOD:
            return new Token(TokenType.MOD, test, currentLine, currentCol);

            case OPEN_PAREN:
            return new Token(TokenType.OPEN_PAREN, test, currentLine, currentCol);

            case CLOSE_PAREN:
            return new Token(TokenType.CLOSE_PAREN, test, currentLine, currentCol);

            case EQUALS:
            return new Token(TokenType.EQUALS, test, currentLine, currentCol);

            case SEMICOLON:
            return new Token(TokenType.SEMICOLON, test, currentLine, currentCol);

            default:
            // Test multi-character strings
            switch (test)
            {
                case EXIT:
                return new Token(TokenType.EXIT, test, currentLine, currentCol - test.length());

                case TYPE_INT:
                return new Token(TokenType.TYPE_INT, test, currentLine, currentCol - test.length());

                default:
                if (isIdentifier(test))
                {
                    return new Token(TokenType.IDENTIFIER, test, currentLine, currentCol - test.length());
                }

                if (isIntLiteral(test))
                {
                    return new Token(TokenType.LITERAL_INT, test, currentLine, currentCol - test.length());
                }
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
}
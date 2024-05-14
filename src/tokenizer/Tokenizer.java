package tokenizer;

import java.util.List;
import java.util.ArrayList;

public class Tokenizer {
    private List<Token> tokenList = new ArrayList<Token>();
    private String str;
    private int strPos;
    private int currentLine;
    private int currentCol;

    public static final char NULL_CHAR = '\0';
    public static final char UNDERSCORE = '_';

    public static final char PLUS = '+';
    public static final char MINUS = '-';
    public static final char TIMES = '*';
    public static final char DIVISION = '/';
    public static final char MOD = '%';
    public static final char DOT = '.';
    public static final char EQUALS = '=';
    public static final char OPEN_PAREN = '(';
    public static final char CLOSE_PAREN = ')';
    public static final char SINGLE_QUOTE = '\'';
    public static final char DOUBLE_QUOTE = '\"';
    public static final char BACKSLASH = '\\';
    public static final char HASHTAG = '#';
    public static final char OPEN_BRACKET = '[';
    public static final char CLOSE_BRACKET = ']';
    public static final char OPEN_BRACE = '{';
    public static final char CLOSE_BRACE = '}';
    public static final char COMMA = ',';
    public static final char SEMICOLON = ';';
    public static final char ENDLINE = '\n';
    public static final char TAB = '\t';

    public static final String EXIT = "exit";
    public static final String PRINT = "print";
    public static final String TYPE_INT = "int";
    public static final String TYPE_FLOAT = "float";
    public static final String TYPE_CHAR = "char";
    public static final String TYPE_STRING = "str";
    public static final String TYPE_VOID = "void";
    public static final String RETURN = "return";

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
                case OPEN_BRACE:
                case CLOSE_BRACE:
                case COMMA:
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

                // tokenize character literal
                case SINGLE_QUOTE:
                if (buffer.length() > 0)
                {
                    tokenList.add(getTokenFromString(buffer));
                    buffer = "";
                }

                int quoteLine = currentLine;
                int quoteCol = currentCol;

                consume();
                
                char literalValue = consumeCharacter();

                // check for closing quote
                if (peek() == SINGLE_QUOTE)
                {
                    consume();
                    tokenList.add(new Token(TokenType.LITERAL_CHAR, "" + literalValue, quoteLine, quoteCol));
                }
                else
                {
                    throw new TokenException("No single quote found to close the one at line " + quoteLine + ", col " + quoteCol);
                }
                break;

                case DOUBLE_QUOTE:
                if (buffer.length() > 0)
                {
                    tokenList.add(getTokenFromString(buffer));
                    buffer = "";
                }

                int dQuoteLine = currentLine;
                int dQuoteCol = currentCol;
                String stringLiteral = "";

                consume();

                while (peek() != NULL_CHAR && peek() != DOUBLE_QUOTE)
                {
                    stringLiteral += consumeCharacter();
                }

                if (peek() == NULL_CHAR)
                {
                    throw new TokenException("No double quote found to close the one at line " + dQuoteLine + ", col " + dQuoteCol);
                }

                consume();
                tokenList.add(new Token(TokenType.LITERAL_STR, "" + stringLiteral, dQuoteLine, dQuoteCol));
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

    public char consumeCharacter() throws TokenException
    {
        char literalValue = consume();
        char next = peek();

        // check for escape sequences
        if (literalValue == BACKSLASH)
        {
            switch (next)
            {
                case 'n':
                consume();
                return ENDLINE;

                case 't':
                consume();
                return TAB;

                case '0':
                consume();
                return NULL_CHAR;

                case SINGLE_QUOTE:
                case DOUBLE_QUOTE:
                case BACKSLASH:
                consume();
                return next;

                default:
                throw new TokenException("Unknown escape sequence: \\" + next);
            }
        }

        return literalValue;
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

            case OPEN_BRACE:
            return new Token(TokenType.OPEN_BRACE, test, currentLine, currentCol);

            case CLOSE_BRACE:
            return new Token(TokenType.CLOSE_BRACE, test, currentLine, currentCol);

            case SINGLE_QUOTE:
            return new Token(TokenType.SINGLE_QUOTE, test, currentLine, currentCol);

            case EQUALS:
            return new Token(TokenType.EQUALS, test, currentLine, currentCol);

            case COMMA:
            return new Token(TokenType.COMMA, test, currentLine, currentCol);

            case SEMICOLON:
            return new Token(TokenType.SEMICOLON, test, currentLine, currentCol);

            default:
            // Test multi-character strings
            switch (test)
            {
                case EXIT:
                return new Token(TokenType.EXIT, test, currentLine, currentCol - test.length());

                case PRINT:
                return new Token(TokenType.PRINT, test, currentLine, currentCol - test.length());

                case RETURN:
                return new Token(TokenType.RETURN, test, currentLine, currentCol - test.length());

                case TYPE_INT:
                case TYPE_FLOAT:
                case TYPE_CHAR:
                case TYPE_STRING:
                case TYPE_VOID:
                return new Token(TokenType.TYPE, test, currentLine, currentCol - test.length());

                default:
                if (isIdentifier(test))
                {
                    return new Token(TokenType.IDENTIFIER, test, currentLine, currentCol - test.length());
                }

                if (isIntLiteral(test))
                {
                    return new Token(TokenType.LITERAL_INT, test, currentLine, currentCol - test.length());
                }

                if (isFloatLiteral(test))
                {
                    return new Token(TokenType.LITERAL_FLOAT, test, currentLine, currentCol - test.length());
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

    private boolean isFloatLiteral(String test)
    {
        for (int i = 0; i < test.length(); i++)
        {
            if (!Character.isDigit(test.charAt(i)) && test.charAt(i) != DOT)
            {
                return false;
            }
            else if (test.charAt(i) == DOT && i == test.length() - 1)
            {
                return false;
            }
        }

        return true;
    }
}
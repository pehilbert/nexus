package parser;

import tokenizer.Token;

public class ParseException extends Exception
{
    public ParseException(String message, Token offendingToken)
    {
        super("Line " + offendingToken.getLine() + ", Col " + offendingToken.getCol() + ": " + message);
    }

    public ParseException(String message)
    {
        super(message);
    }
}
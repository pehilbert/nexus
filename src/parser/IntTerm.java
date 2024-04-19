package parser;

import tokenizer.Token;

public class IntTerm
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

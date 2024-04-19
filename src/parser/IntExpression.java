package parser;
import tokenizer.Token;

public class IntExpression
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
package parser;

import tokenizer.Token;

public class IntFactor {
    private Token token;
    private IntExpression expr;

    public IntFactor(Token inToken)
    {
        token = inToken;
        expr = null;
    }

    public IntFactor(IntExpression inExpr)
    {
        expr = inExpr;
        token = null;
    }

    public Token getToken()
    {
        return token;
    }

    public IntExpression getExpression()
    {
        return expr;
    }

    public String toString()
    {
        if (token != null)
        {
            return token.getValue();
        }

        return expr.toString();
    }
}

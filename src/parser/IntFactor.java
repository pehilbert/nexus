package parser;

import tokenizer.Token;

public class IntFactor {
    private Token token;
    private IntExpression expr;
    private boolean negative;

    public IntFactor(Token inToken, boolean neg)
    {
        token = inToken;
        expr = null;
        negative = neg;
    }

    public IntFactor(IntExpression inExpr, boolean neg)
    {
        expr = inExpr;
        token = null;
        negative = neg;
    }

    public Token getToken()
    {
        return token;
    }

    public IntExpression getExpression()
    {
        return expr;
    }

    public boolean isNegative()
    {
        return negative;
    }

    public String toString()
    {
        String s = "";

        if (negative)
        {
            s += "-";
        }

        if (token != null)
        {
            return s + token.getValue();
        }

        return s + expr.toString();
    }
}

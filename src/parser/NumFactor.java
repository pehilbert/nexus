package parser;

import tokenizer.Token;

public class NumFactor {
    private Token token;
    private NumExpression expr;
    private boolean negative;
    private boolean floatExpr;

    public NumFactor(Token inToken, boolean neg)
    {
        token = inToken;
        expr = null;
        negative = neg;
        floatExpr = false;
    }

    public NumFactor(NumExpression inExpr, boolean neg)
    {
        expr = inExpr;
        token = null;
        negative = neg;
        floatExpr = inExpr.isFloat();
    }

    public Token getToken()
    {
        return token;
    }

    public NumExpression getExpression()
    {
        return expr;
    }

    public boolean isNegative()
    {
        return negative;
    }

    public void setFloat(boolean inBool)
    {
        floatExpr = inBool;
    }

    public boolean isFloat()
    {
        return floatExpr;
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

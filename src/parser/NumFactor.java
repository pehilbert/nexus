package parser;

import tokenizer.Tokenizer;
import tokenizer.Token;

public class NumFactor {
    private Token token;
    private NumExpression expr;
    private FunctionCall functionCall;
    private boolean negative;
    private boolean floatExpr;

    public NumFactor(Token inToken, boolean neg)
    {
        token = inToken;
        expr = null;
        functionCall = null;
        negative = neg;
        floatExpr = false;
    }

    public NumFactor(NumExpression inExpr, boolean neg)
    {
        expr = inExpr;
        token = null;
        functionCall = null;
        negative = neg;
        floatExpr = inExpr.isFloat();
    }

    public NumFactor(FunctionCall function, boolean neg)
    {
        functionCall = function;
        token = null;
        expr = null;
        negative = neg;
        floatExpr = function.getReturnType().equals(Tokenizer.TYPE_FLOAT);
    }

    public Token getToken()
    {
        return token;
    }

    public NumExpression getExpression()
    {
        return expr;
    }

    public FunctionCall getFunctionCall()
    {
        return functionCall;
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

        if (expr != null)
        {
            return s + expr.toString();
        }

        return s + functionCall.toString();
    }
}

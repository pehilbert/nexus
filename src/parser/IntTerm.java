package parser;

import tokenizer.Token;

public class IntTerm
{
    private IntFactor lhs;
    private Token operator;
    private IntFactor rhs;

    public IntTerm(IntFactor left, Token inOperator, IntFactor right)
    {
        lhs = left;
        operator = inOperator;
        rhs = right;
    }

    public IntTerm(IntFactor inFactor)
    {
        lhs = inFactor;
        operator = null;
        rhs = null;
    }

    public IntFactor getLeft()
    {
        return lhs;
    }

    public Token getOperator()
    {
        return operator;
    }

    public IntFactor getRight()
    {
        return rhs;
    }

    public String toString()
    {
        if (operator != null)
        {
            return lhs.toString() + " " + operator.getValue() + " " + rhs.toString();
        }

        return lhs.toString();
    }
}

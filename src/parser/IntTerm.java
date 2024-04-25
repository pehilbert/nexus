package parser;

import tokenizer.Token;

public class IntTerm
{
    private IntFactor factor;
    private IntTerm lhs;
    private Token operator;
    private IntTerm rhs;

    public IntTerm(IntTerm left, Token inOperator, IntTerm right)
    {
        lhs = left;
        operator = inOperator;
        rhs = right;
        factor = null;
    }

    public IntTerm(IntFactor inFactor)
    {
        factor = inFactor;
        lhs = null;
        operator = null;
        rhs = null;
    }

    public IntFactor getFactor()
    {
        return factor;
    }

    public IntTerm getLeft()
    {
        return lhs;
    }

    public Token getOperator()
    {
        return operator;
    }

    public IntTerm getRight()
    {
        return rhs;
    }

    public String toString()
    {
        if (factor != null)
        {
            return factor.toString();
        }

        return "(" + lhs.toString() + " " + operator.getValue() + " " + rhs.toString() + ")";
    }
}

package parser;

import tokenizer.Token;

public class NumTerm
{
    private NumFactor factor;
    private NumTerm lhs;
    private Token operator;
    private NumTerm rhs;
    private boolean floatExpr;

    public NumTerm(NumTerm left, Token inOperator, NumTerm right)
    {
        lhs = left;
        operator = inOperator;
        rhs = right;
        factor = null;
        floatExpr = left.isFloat() || right.isFloat();
    }

    public NumTerm(NumFactor inFactor)
    {
        factor = inFactor;
        lhs = null;
        operator = null;
        rhs = null;
        floatExpr = factor.isFloat();
    }

    public NumFactor getFactor()
    {
        return factor;
    }

    public NumTerm getLeft()
    {
        return lhs;
    }

    public Token getOperator()
    {
        return operator;
    }

    public NumTerm getRight()
    {
        return rhs;
    }

    public boolean isFloat()
    {
        return floatExpr;
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

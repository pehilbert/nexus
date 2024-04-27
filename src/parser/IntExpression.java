package parser;

import tokenizer.Token;

public class IntExpression implements Expression
{
    private IntTerm term;
    private IntExpression lhs;
    private Token operator;
    private IntExpression rhs;

    public IntExpression(IntTerm inTerm)
    {
        term = inTerm;
    }

    public IntExpression(IntExpression left, Token inOperator, IntExpression right)
    {
        lhs = left;
        operator = inOperator;
        rhs = right;
    }

    public IntExpression getLeft()
    {
        return lhs;
    }

    public Token getOperator()
    {
        return operator;
    }

    public IntExpression getRight()
    {
        return rhs;
    }

    public IntTerm getTerm()
    {
        return term;
    }

    public String toString()
    {
        if (term == null)
        {
            return "(" + lhs.toString() + " " + operator.getValue() + " " + rhs.toString() + ")";
        }

        return term.toString();
    }
}
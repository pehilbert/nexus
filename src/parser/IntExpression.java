package parser;

import tokenizer.Token;

public class IntExpression
{
    private IntExpression expr;
    private Token operator;
    private IntTerm term;

    public IntExpression(IntTerm inTerm)
    {
        term = inTerm;
    }

    public IntExpression(IntExpression inExpr, Token inOperator, IntTerm inTerm)
    {
        expr = inExpr;
        operator = inOperator;
        term = inTerm;
    }

    public IntExpression getExpression()
    {
        return expr;
    }

    public Token getOperator()
    {
        return operator;
    }

    public IntTerm getTerm()
    {
        return term;
    }

    public String toString()
    {
        if (expr != null)
        {
            return term.toString() + " " + operator.getValue() + " " + expr.toString();
        }

        return term.toString();
    }
}
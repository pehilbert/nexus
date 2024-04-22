package parser;

import tokenizer.Token;

public class IntTerm
{
    private IntTerm term;
    private Token operator;
    private IntFactor factor;

    public IntTerm(IntTerm inTerm, Token inOperator, IntFactor inFactor)
    {
        term = inTerm;
        operator = inOperator;
        factor = inFactor;
    }

    public IntTerm(IntFactor inFactor)
    {
        term = null;
        operator = null;
        factor = inFactor;
    }

    public IntTerm getTerm()
    {
        return term;
    }

    public Token getOperator()
    {
        return operator;
    }

    public IntFactor getFactor()
    {
        return factor;
    }

    public String toString()
    {
        if (term != null)
        {
            return factor.toString() + " " + operator.getValue() + " " + term.toString();
        }

        return factor.toString();
    }
}

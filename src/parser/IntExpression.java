package parser;

public class IntExpression
{
    private IntTerm term;

    public IntExpression(IntTerm inTerm)
    {
        term = inTerm;
    }

    public IntTerm getTerm()
    {
        return term;
    }
}
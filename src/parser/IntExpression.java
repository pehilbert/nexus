package parser;
import tokenizer.Token;

public class IntExpression
{
    private IntTerm term;

    public IntExpression(IntTerm inTerm)
    {
        term = inTerm;
    }

    public String evaluate(String targetRegister) throws ParseException
    {
        String assembly = "";
        switch (term.getToken().getType())
        {
            case LITERAL_INT:
            assembly += "\tmov " + targetRegister + ", " + term.getToken().getValue() + "\n";
            return assembly;

            case IDENTIFIER:
            assembly += "\tmov " + targetRegister + ", 0\n";
            return assembly;

            default:
            throw new ParseException("Expected an int value, instead got " + term.getToken().getValue());
        }
    }

    public IntTerm getTerm()
    {
        return term;
    }
}

class IntTerm
{
    private Token token;

    public IntTerm(Token inToken)
    {
        token = inToken;
    }

    public Token getToken()
    {
        return token;
    }
}
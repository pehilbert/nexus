package parser;

import tokenizer.Token;

public interface Statement 
{
    public void printStatement();
    public String getAssembly();
}

class IntDeclaration implements Statement 
{
    private Token identifierToken;
    private IntExpression expression;

    public IntDeclaration(Token identifier, IntExpression expr)
    {
        identifierToken = identifier;
        expression = expr;
    }

    public void printStatement()
    {
        System.out.println("Int declaration: " + identifierToken.getValue() + 
                            ", " + expression.getTerm().getToken().getValue());
    }

    public String getAssembly()
    {
        String assembly = "";
        
        try
        {
            assembly += identifierToken.getValue() + ":\n";
            assembly += "\tmov eax, " + expression.evaluate() + "\n";
            assembly += "\tpush eax\n";
        }
        catch (ParseException exception)
        {
            exception.printStackTrace();
        }

        return assembly;
    }

    public Token getIdentifier()
    {
        return identifierToken;
    }

    public IntExpression getExpression()
    {
        return expression;
    }
}

class ExitStatement implements Statement 
{
    IntExpression expression;

    public ExitStatement(IntExpression expr)
    {
        expression = expr;
    }

    public void printStatement()
    {
        System.out.println("Exit statement: " + 
                            expression.getTerm().getToken().getValue());
    }

    public String getAssembly()
    {
        String assembly = "";

        try
        {
            assembly += "\tmov eax, 1\n";
            assembly += "\tmov ebx, " + expression.evaluate() + "\n";
            assembly += "\tint 0x80\n";
        }
        catch (ParseException exception)
        {
            exception.printStackTrace();
        }

        return assembly;
    }

    public IntExpression getExpression()
    {
        return expression;
    }
}

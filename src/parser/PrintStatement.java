package parser;

import codegen.CompileException;
import codegen.AssemblyVisitor;

public class PrintStatement implements Statement 
{
    StringExpression expression;

    public PrintStatement(StringExpression expr)
    {
        expression = expr;
    }

    public void printStatement()
    {
        System.out.println("Print statement: " + expression.toString());
    }

    public StringExpression getExpression()
    {
        return expression;
    }

    public String accept(AssemblyVisitor visitor) throws CompileException
    {
        try
        {
            return visitor.visit(this);
        }
        catch (CompileException exception)
        {
            throw exception;
        }
    }
}
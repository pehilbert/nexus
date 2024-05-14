package parser;

import codegen.CompileException;
import codegen.AssemblyVisitor;

public class ExitStatement implements Statement 
{
    NumExpression expression;

    public ExitStatement(NumExpression expr)
    {
        expression = expr;
    }

    public void printStatement()
    {
        System.out.println("Exit statement: " + expression.toString());
    }

    public NumExpression getExpression()
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
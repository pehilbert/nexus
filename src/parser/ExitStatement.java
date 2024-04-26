package parser;

import codegen.CompileException;
import codegen.StatementVisitor;

public class ExitStatement implements Statement 
{
    IntExpression expression;

    public ExitStatement(IntExpression expr)
    {
        expression = expr;
    }

    public void printStatement()
    {
        System.out.println("Exit statement: " + expression.toString());
    }

    public IntExpression getExpression()
    {
        return expression;
    }

    public String accept(StatementVisitor visitor) throws CompileException
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
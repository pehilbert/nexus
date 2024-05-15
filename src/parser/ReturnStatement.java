package parser;

import codegen.CompileException;
import codegen.AssemblyVisitor;

public class ReturnStatement implements Statement {
    private Expression expression;

    public ReturnStatement(Expression expr)
    {
        expression = expr;
    }

    public Expression getExpression()
    {
        return expression;
    }

    public void printStatement()
    {
        System.out.println("Return statement: " + expression.toString());
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

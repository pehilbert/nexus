package parser;

import codegen.CompileException;
import codegen.StatementVisitor;

public class ReturnStatement implements Statement {
    private Expression expression;

    public ReturnStatement(Expression expr)
    {
        expression = expr;
    }

    public void printStatement()
    {
        System.out.println("Return statement: " + expression.toString());
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

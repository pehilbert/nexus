package parser;

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
        System.out.println("Exit statement: " + 
                            expression.getTerm().getToken().getValue());
    }

    public IntExpression getExpression()
    {
        return expression;
    }

    public String accept(StatementVisitor visitor)
    {
        return visitor.visit(this);
    }
}
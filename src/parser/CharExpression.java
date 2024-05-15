package parser;

import codegen.AssemblyVisitor;
import codegen.CompileException;

public class CharExpression implements Expression {
    private NumExpression expr;

    public CharExpression(NumExpression numExpr)
    {
        expr = numExpr;
    }

    public NumExpression getExpression()
    {
        return expr;
    }

    public String asmRegister()
    {
        return "bl";
    }

    public String toString()
    {
        return expr.toString();
    }

    public String accept(AssemblyVisitor visitor) throws CompileException
    {
        return visitor.visit(this, asmRegister());
    }
}

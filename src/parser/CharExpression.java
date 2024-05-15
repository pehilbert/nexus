package parser;

import codegen.AssemblyGenerator;
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
        return AssemblyGenerator.CHAR_EXPR_REGISTER;
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

package parser;

import codegen.AssemblyGenerator;
import codegen.AssemblyVisitor;
import codegen.CompileException;
import tokenizer.Token;

public class NumExpression implements Expression
{
    private NumTerm term;
    private NumExpression lhs;
    private Token operator;
    private NumExpression rhs;
    private boolean floatExpr;

    public NumExpression(NumTerm inTerm)
    {
        term = inTerm;
        floatExpr = inTerm.isFloat();
    }

    public NumExpression(NumExpression left, Token inOperator, NumExpression right)
    {
        lhs = left;
        operator = inOperator;
        rhs = right;
        floatExpr = left.isFloat() || right.isFloat();
    }

    public NumExpression getLeft()
    {
        return lhs;
    }

    public Token getOperator()
    {
        return operator;
    }

    public NumExpression getRight()
    {
        return rhs;
    }

    public NumTerm getTerm()
    {
        return term;
    }

    public boolean isFloat()
    {
        return floatExpr;
    }

    public String asmRegister()
    {
        if (floatExpr)
        {
            return AssemblyGenerator.FLOAT_EXPR_REGISTER;
        }

        return AssemblyGenerator.INT_EXPR_REGISTER;
    }

    public String accept(AssemblyVisitor visitor) throws CompileException
    {
        return visitor.visit(this, asmRegister(), floatExpr);
    }

    public String toString()
    {
        String floatPrefix = "(Float = " + Boolean.toString(floatExpr) + ") ";

        if (term == null)
        {
            return floatPrefix + "(" + lhs.toString() + " " + operator.getValue() + " " + rhs.toString() + ")";
        }

        return floatPrefix + term.toString();
    }
}
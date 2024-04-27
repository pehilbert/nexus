package parser;

import codegen.CompileException;
import codegen.StatementVisitor;
import tokenizer.Token;

public class IntReassignment extends Reassignment {
    private IntExpression expression;

    public IntReassignment(Token identifier, IntExpression expr)
    {
        setIdentifier(identifier);
        expression = expr;
    }

    public final IntExpression getExpression()
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
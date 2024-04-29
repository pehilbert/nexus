package parser;

import codegen.CompileException;
import codegen.StatementVisitor;

import tokenizer.Token;

public class StringReassignment extends Reassignment {
    private StringExpression expression;

    public StringReassignment(Token identifier, StringExpression expr)
    {
        setIdentifier(identifier);
        expression = expr;
    }

    public final StringExpression getExpression()
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

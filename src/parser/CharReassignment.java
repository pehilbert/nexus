package parser;

import codegen.CompileException;
import codegen.StatementVisitor;
import tokenizer.Token;

public class CharReassignment extends Reassignment {
    private NumExpression expression;

    public CharReassignment(Token identifier, NumExpression expr)
    {
        setIdentifier(identifier);
        expression = expr;
    }

    public final NumExpression getExpression()
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
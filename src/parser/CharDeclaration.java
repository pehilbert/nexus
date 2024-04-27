package parser;

import tokenizer.Token;
import codegen.StatementVisitor;
import codegen.CompileException;

public class CharDeclaration extends Declaration
{
    private IntExpression expression;

    public CharDeclaration(Token type, Token identifier, IntExpression expr)
    {
        setType(type);
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

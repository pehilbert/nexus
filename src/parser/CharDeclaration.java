package parser;

import tokenizer.Token;
import codegen.StatementVisitor;
import codegen.CompileException;

public class CharDeclaration extends Declaration
{
    private NumExpression expression;

    public CharDeclaration(Token type, Token identifier, NumExpression expr)
    {
        setType(type);
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

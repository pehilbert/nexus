package parser;

import codegen.CompileException;
import codegen.StatementVisitor;
import tokenizer.Token;

public class StringDeclaration extends Declaration {
    private StringExpression expression;

    public StringDeclaration(Token type, Token identifier, StringExpression expr)
    {
        setType(type);
        setIdentifier(identifier);
        expression = expr;
    }

    public StringExpression getExpression()
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

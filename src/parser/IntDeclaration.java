package parser;

import tokenizer.Token;
import codegen.StatementVisitor;
import codegen.CompileException;

public class IntDeclaration implements Statement 
{
    private Token identifierToken;
    private IntExpression expression;

    public IntDeclaration(Token identifier, IntExpression expr)
    {
        identifierToken = identifier;
        expression = expr;
    }

    public void printStatement()
    {
        System.out.println("Int declaration: " + expression.toString());
    }

    public Token getIdentifier()
    {
        return identifierToken;
    }

    public IntExpression getExpression()
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

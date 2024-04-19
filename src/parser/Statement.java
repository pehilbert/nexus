package parser;

import tokenizer.Token;

public interface Statement 
{
    public void printStatement();
}

class IntDeclaration implements Statement 
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
        System.out.println("Int declaration: " + identifierToken.getValue() + 
                            ", " + expression.getTerm().getToken().getValue());
    }

    public Token getIdentifier()
    {
        return identifierToken;
    }

    public IntExpression getExpression()
    {
        return expression;
    }
}

class ExitStatement implements Statement 
{
    IntExpression expression;

    public ExitStatement(IntExpression expr)
    {
        expression = expr;
    }

    public void printStatement()
    {
        System.out.println("Exit statement: " + 
                            expression.getTerm().getToken().getValue());
    }

    public IntExpression getExpression()
    {
        return expression;
    }
}

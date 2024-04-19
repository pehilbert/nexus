package parser;

import tokenizer.Token;
import codegen.StatementVisitor;

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

    public String accept(StatementVisitor visitor)
    {
        return visitor.visit(this);
    }
}

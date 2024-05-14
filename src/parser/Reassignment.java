package parser;

import codegen.CompileException;
import codegen.AssemblyVisitor;
import tokenizer.Token;

public class Reassignment implements Statement {
    private Token identifierToken;
    private Expression expression;

    public Reassignment(Token identifier, Expression expr)
    {
        identifierToken = identifier;
        expression = expr;
    }

    public Token getIdentifier()
    {
        return identifierToken;
    }

    public void setIdentifier(Token identifier)
    {
        identifierToken = identifier;
    }

    public Expression getExpression()
    {
        return expression;
    }

    public void printStatement()
    {
        System.out.println(identifierToken.getValue() + " reassigned to " + getExpression().toString());
    }

    public String accept(AssemblyVisitor visitor) throws CompileException
    {
        return visitor.visit(this);
    }
}

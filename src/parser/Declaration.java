package parser;

import codegen.CompileException;
import codegen.AssemblyVisitor;
import tokenizer.Token;

public class Declaration implements Statement {
    private Token typeToken;
    private Token identifierToken;
    private Expression expression;

    public Declaration(Token type, Token identifier, Expression expr)
    {
        typeToken = type;
        identifierToken = identifier;
        expression = expr;
    }

    public Token getType()
    {
        return typeToken;
    }

    public Token getIdentifier()
    {
        return identifierToken;
    }

    public Expression getExpression()
    {
        return expression;
    }

    public void printStatement()
    {
        System.out.println(typeToken.getValue() + " declaration: " + identifierToken.getValue() + " = " + getExpression().toString());
    }

    public String accept(AssemblyVisitor visitor) throws CompileException
    {
        return visitor.visit(this);
    }
}

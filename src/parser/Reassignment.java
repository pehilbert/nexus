package parser;

import tokenizer.Token;

public abstract class Reassignment implements Statement {
    private Token identifierToken;

    public Token getIdentifier()
    {
        return identifierToken;
    }

    public void setIdentifier(Token identifier)
    {
        identifierToken = identifier;
    }

    public abstract Expression getExpression();

    public void printStatement()
    {
        System.out.println(identifierToken.getValue() + " reassigned to " + getExpression().toString());
    }
}

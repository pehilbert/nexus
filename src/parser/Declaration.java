package parser;

import tokenizer.Token;

public abstract class Declaration implements Statement {
    private Token typeToken;
    private Token identifierToken;

    public void setType(Token type)
    {
        typeToken = type;
    }

    public void setIdentifier(Token identifier)
    {
        typeToken = identifier;
    }

    public Token getType()
    {
        return typeToken;
    }

    public Token getIdentifier()
    {
        return identifierToken;
    }

    public abstract Expression getExpression();

    public void printStatement()
    {
        System.out.println(typeToken.getValue() + " declaration: " + identifierToken.getValue() + " = " + getExpression().toString());
    }

}

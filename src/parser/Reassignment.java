package parser;

import tokenizer.Token;

public abstract class Reassignment {
    private Token identifierToken;

    public Token getIdentifier()
    {
        return identifierToken;
    }

    public abstract Expression getExpression();
}

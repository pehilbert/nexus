package parser;

import tokenizer.Token;
import tokenizer.TokenType;

public class StringExpression implements Expression {
    private Token token;

    public StringExpression(Token inToken)
    {
        token = inToken;
    }

    public Token getToken()
    {
        return token;
    }

    public String toString()
    {
        if (token.getType() == TokenType.LITERAL_STR)
        {
            return "\"" + token.getValue() + "\"";
        }

        return token.getValue();
    }
}

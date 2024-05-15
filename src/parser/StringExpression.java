package parser;

import tokenizer.Token;
import tokenizer.TokenType;

import codegen.AssemblyVisitor;
import codegen.CompileException;

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

    public String accept(AssemblyVisitor visitor) throws CompileException
    {
        return visitor.visit(this, asmRegister());
    }

    public String asmRegister()
    {
        return "eax";
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

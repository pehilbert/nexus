package parser;

import tokenizer.Token;
import tokenizer.TokenType;

import codegen.AssemblyGenerator;
import codegen.AssemblyVisitor;
import codegen.CompileException;

public class StringExpression implements Expression {
    private Token token;
    private FunctionCall functionCall;

    public StringExpression(Token inToken)
    {
        token = inToken;
        functionCall = null;
    }

    public StringExpression(FunctionCall function)
    {
        functionCall = function;
        token = null;
    }

    public Token getToken()
    {
        return token;
    }

    public FunctionCall getFunctionCall()
    {
        return functionCall;
    }

    public String accept(AssemblyVisitor visitor) throws CompileException
    {
        return visitor.visit(this, asmRegister());
    }

    public String asmRegister()
    {
        return AssemblyGenerator.STR_EXPR_REGISTER;
    }

    public String toString()
    {
        if (token != null)
        {
            if (token.getType() == TokenType.LITERAL_STR)
            {
                return "\"" + token.getValue() + "\"";
            }

            return token.getValue();
        }

        return functionCall.toString();
    }
}

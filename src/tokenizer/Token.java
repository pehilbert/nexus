package tokenizer;

public class Token 
{
    private TokenType type;
    private String value;
    private int pos;

    public Token(TokenType type, String value, int pos) 
    {
        this.type = type;
        this.value = value;
        this.pos = pos;
    }

    // Getter methods
    public TokenType getType() 
    {
        return type;
    }

    public String getValue() 
    {
        return value;
    }

    public int getPos() 
    {
        return pos;
    }
}
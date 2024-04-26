package tokenizer;

public class Token 
{
    private TokenType type;
    private String value;
    private int line;
    private int col;

    public Token(TokenType type, String value, int line, int col) 
    {
        this.type = type;
        this.value = value;
        this.line = line;
        this.col = col;
    }

    public TokenType getType() 
    {
        return type;
    }

    public String getValue() 
    {
        return value;
    }

    public int getLine() 
    {
        return line;
    }

    public int getCol()
    {
        return col;
    }

    public String toString()
    {
        return type + ", " + value + " (line " + line + ", col " + col + ")";
    }
}
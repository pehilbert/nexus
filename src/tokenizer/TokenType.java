package tokenizer;

public enum TokenType 
{
    TYPE,
    LITERAL_INT,
    LITERAL_FLOAT,
    LITERAL_CHAR,
    LITERAL_STR,

    IDENTIFIER, 
    EXIT,
    PRINT,

    PLUS,
    MINUS,
    TIMES,
    DIVISION,
    MOD,
    OPEN_PAREN,
    CLOSE_PAREN,
    OPEN_BRACE,
    CLOSE_BRACE,
    EQUALS,
    SEMICOLON,
    SINGLE_QUOTE
}

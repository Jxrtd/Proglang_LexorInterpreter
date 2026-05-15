package com.lexor.lexer;

public class Token {
    public final TokenType type;
    public final String lexeme;
    public final Object literal;
    public final int line;

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    // Displays the token for debugging purposes.
    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
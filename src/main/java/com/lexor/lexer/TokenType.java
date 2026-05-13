package com.lexor.lexer;

// Defines all valid token types in the LEXOR language for Increment 1.
public enum TokenType {
    SCRIPT, AREA, START, END, DECLARE, PRINT, SCAN,
    INT, FLOAT, CHAR, BOOL,
    IDENTIFIER, STRING, NUMBER, BOOL_LITERAL,
    EQUAL, AMPERSAND, COLON, DOLLAR, COMMA, MINUS,
    LEFT_PAREN, RIGHT_PAREN, 
    GREATER, GREATER_EQUAL, LESS, LESS_EQUAL, EQUAL_EQUAL,
    EOF
}
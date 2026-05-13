package com.lexor.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lexor.core.LexorException;

// Scans raw source code character-by-character and produces a list of Tokens.
public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords = new HashMap<>();

    static {
        keywords.put("SCRIPT", TokenType.SCRIPT);
        keywords.put("AREA", TokenType.AREA);
        keywords.put("START", TokenType.START);
        keywords.put("END", TokenType.END);
        keywords.put("DECLARE", TokenType.DECLARE);
        keywords.put("PRINT", TokenType.PRINT);
        keywords.put("SCAN", TokenType.SCAN);
        keywords.put("INT", TokenType.INT);
        keywords.put("FLOAT", TokenType.FLOAT);
        keywords.put("CHAR", TokenType.CHAR);
        keywords.put("BOOL", TokenType.BOOL);
    }

    // Normalizes quotations and prepares the source for scanning.
    public Lexer(String source) {
        this.source = source.replace("“", "\"").replace("”", "\"").replace("‘", "'").replace("’", "'");
    }

    // Loops until EOF, scanning each token in the script.
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    // Identifies the next token based on the current character.
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(TokenType.LEFT_PAREN);
            case ')' -> addToken(TokenType.RIGHT_PAREN);
            case '>' -> addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
            case '<' -> addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
            case '=' -> {
                if (match('=')) addToken(TokenType.EQUAL_EQUAL);
                else addToken(TokenType.EQUAL);
            }
            case '&' -> addToken(TokenType.AMPERSAND);
            case ':' -> addToken(TokenType.COLON);
            case '$' -> addToken(TokenType.DOLLAR);
            case ',' -> addToken(TokenType.COMMA);
            case '-' -> addToken(TokenType.MINUS);
            case ' ', '\r', '\t' -> {}
            case '\n' -> line++;
            case '%' -> {
                if (match('%')) while (peek() != '\n' && !isAtEnd()) advance();
                else throw new LexorException(line, "Unexpected character: %");
            }
            case '"', '\'' -> string(c);
            case '[' -> escapeCode();
            default -> {
                if (Character.isDigit(c)) number();
                else if (Character.isLetter(c) || c == '_') identifier();
                else throw new LexorException(line, "Unexpected character: " + c);
            }
        }
    }

    // Captures content inside brackets as a string literal (e.g., [#]).
    private void escapeCode() {
        if (peek() == ']' && peekNext() == ']') { // Handle []] case
            advance();
            advance();
            addToken(TokenType.STRING, "]");
            return;
        }
        while (peek() != ']' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) throw new LexorException(line, "Unterminated escape code.");
        advance();
        String value = source.substring(start + 1, current - 1);
        if (value.equals("[")) addToken(TokenType.STRING, "[");
        else addToken(TokenType.STRING, value);
    }

    // Parses string literals, recognizing BOOL literals inside quotes.
    private void string(char quoteChar) {
        while (peek() != quoteChar && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) throw new LexorException(line, "Unterminated string.");
        advance();
        String value = source.substring(start + 1, current - 1);
        if (value.equals("TRUE")) addToken(TokenType.BOOL_LITERAL, true);
        else if (value.equals("FALSE")) addToken(TokenType.BOOL_LITERAL, false);
        else addToken(TokenType.STRING, value);
    }

    // Parses numeric sequences into number tokens.
    private void number() {
        while (Character.isDigit(peek())) advance();
        if (peek() == '.' && Character.isDigit(peekNext())) {
            advance();
            while (Character.isDigit(peek())) advance();
            addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
        } else {
            addToken(TokenType.NUMBER, Integer.parseInt(source.substring(start, current)));
        }
    }

    // Parses names and identifies reserved keywords like DECLARE.
    private void identifier() {
        while (Character.isLetterOrDigit(peek()) || peek() == '_') advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) {
            if (text.equals("TRUE")) addToken(TokenType.BOOL_LITERAL, true);
            else if (text.equals("FALSE")) addToken(TokenType.BOOL_LITERAL, false);
            else addToken(TokenType.IDENTIFIER, text);
        } else {
            addToken(type);
        }
    }

    // Moves the scanner forward by one character.
    private char advance() { return source.charAt(current++); }

    // Advances only if the current character matches expectations.
    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    // Returns the current character without consuming it.
    private char peek() { return isAtEnd() ? '\0' : source.charAt(current); }

    // Returns the next character without consuming it.
    private char peekNext() { return current + 1 >= source.length() ? '\0' : source.charAt(current + 1); }

    // Checks if the scanner has reached the end of file.
    private boolean isAtEnd() { return current >= source.length(); }

    // Adds a token without an associated literal value.
    private void addToken(TokenType type) { addToken(type, null); }

    // Adds a token with its literal value.
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
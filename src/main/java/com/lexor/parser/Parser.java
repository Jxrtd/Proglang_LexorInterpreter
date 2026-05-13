package com.lexor.parser;

import com.lexor.ast.Expr;
import com.lexor.ast.Stmt;
import com.lexor.core.LexorException;
import com.lexor.lexer.Token;
import com.lexor.lexer.TokenType;
import java.util.ArrayList;
import java.util.List;

// Converts a flat list of Tokens into a structured Tree (AST).
public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private boolean declarationPhase = true;

    // Sets up the parser with the scanned tokens.
    public Parser(List<Token> tokens) { this.tokens = tokens; }

    // Parses the full SCRIPT structure: Area, Start, Statements, End.
    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        consume(TokenType.SCRIPT, "Expect 'SCRIPT AREA' at the beginning.");
        consume(TokenType.AREA, "Expect 'SCRIPT AREA' at the beginning.");
        consume(TokenType.START, "Expect 'START SCRIPT'.");
        consume(TokenType.SCRIPT, "Expect 'START SCRIPT'.");
        while (!check(TokenType.END) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(TokenType.END, "Expect 'END SCRIPT'.");
        consume(TokenType.SCRIPT, "Expect 'END SCRIPT'.");
        if (!isAtEnd()) throw error(peek(), "Unexpected tokens after END SCRIPT.");
        return statements;
    }

    // Routes to declaration parsing or statement parsing based on state.
    private Stmt declaration() {
        if (match(TokenType.DECLARE)) {
            if (!declarationPhase) throw error(previous(), "Declarations must follow right after START SCRIPT.");
            return varDeclaration();
        }
        declarationPhase = false;
        return statement();
    }

    // Parses a DECLARE block, verifying the type and names.
    private Stmt varDeclaration() {
        Token type = advance();
        if (type.type != TokenType.INT && type.type != TokenType.FLOAT && type.type != TokenType.CHAR && type.type != TokenType.BOOL) {
            throw error(type, "Expect valid data type (INT, FLOAT, CHAR, BOOL).");
        }
        List<Token> names = new ArrayList<>();
        List<Expr> initializers = new ArrayList<>();
        do {
            names.add(consume(TokenType.IDENTIFIER, "Expect variable name."));
            Expr init = null;
            if (match(TokenType.EQUAL)) init = expression();
            initializers.add(init);
        } while (match(TokenType.COMMA));
        return new Stmt.Declare(type, names, initializers);
    }

    // Identifies the type of statement (currently PRINT or general expression).
    private Stmt statement() {
        if (match(TokenType.PRINT)) return printStatement();
        if (match(TokenType.SCAN)) return scanStatement();
        return expressionStatement();
    }

    // Parses a PRINT statement, collecting all chunks separated by '&'.
    private Stmt printStatement() {
        consume(TokenType.COLON, "Expect ':' after PRINT.");
        List<Expr> expressions = new ArrayList<>();
        do {
            if (match(TokenType.DOLLAR)) expressions.add(new Expr.Literal("$"));
            else expressions.add(expression());
        } while (match(TokenType.AMPERSAND));
        return new Stmt.Print(expressions);
    }

    // Parses a SCAN statement.
    private Stmt scanStatement() {
        consume(TokenType.COLON, "Expect ':' after SCAN.");
        List<Token> names = new ArrayList<>();
        do {
            names.add(consume(TokenType.IDENTIFIER, "Expect variable name after SCAN."));
        } while (match(TokenType.COMMA));
        return new Stmt.Scan(names);
    }

    // Wraps an expression result as a standalone statement.
    private Stmt expressionStatement() {
        return new Stmt.Expression(expression());
    }

    // Standard entry point for expression parsing.
    private Expr expression() {
        return assignment();
    }

    // Handles variable assignments (supporting chains like x = y = 5).
    private Expr assignment() {
        Expr expr = or();
        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Variable varExpr) return new Expr.Assign(varExpr.name, value);
            throw error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    // Parses logical OR.
    private Expr or() {
        Expr expr = and();
        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // Parses logical AND.
    private Expr and() {
        Expr expr = comparison();
        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // Parses comparison operators (>, >=, <, <=, ==, <>).
    private Expr comparison() {
        Expr expr = term();
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL, TokenType.EQUAL_EQUAL, TokenType.LESS_GREATER)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // Parses addition and subtraction (+, -).
    private Expr term() {
        Expr expr = factor();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // Parses multiplication, division, and modulo (*, /, %).
    private Expr factor() {
        Expr expr = unary();
        while (match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // Parses unary operators (currently MINUS, NOT).
    private Expr unary() {
        if (match(TokenType.MINUS, TokenType.NOT)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    // Resolves basic atoms: strings, numbers, booleans, variables, and grouped expressions.
    private Expr primary() {
        if (match(TokenType.BOOL_LITERAL, TokenType.NUMBER, TokenType.STRING)) return new Expr.Literal(previous().literal);
        if (match(TokenType.IDENTIFIER)) return new Expr.Variable(previous());
        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return expr;
        }
        throw error(peek(), "Expect expression.");
    }

    // Checks if the current token matches any expected types and advances.
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    // Ensures the next token matches expectations, or throws an error.
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    // Looks at the type of the current token without moving.
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    // Consumes and returns the current token.
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    // Checks if the parser has reached the end of the token stream.
    private boolean isAtEnd() { return peek().type == TokenType.EOF; }

    // Returns the current token being analyzed.
    private Token peek() { return tokens.get(current); }

    // Returns the token that was just processed.
    private Token previous() { return tokens.get(current - 1); }

    // Formats a high-signal parsing error with line information.
    private LexorException error(Token token, String message) {
        return new LexorException(token.line, message);
    }
}
package com.lexor.parser;

import java.util.ArrayList;
import java.util.List;

import com.lexor.ast.Expr;
import com.lexor.ast.Stmt;
import com.lexor.core.LexorException;
import com.lexor.lexer.Token;
import com.lexor.lexer.TokenType;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private boolean declarationPhase = true;

    public Parser(List<Token> tokens) { this.tokens = tokens; }

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

    private Stmt declaration() {
        if (match(TokenType.DECLARE)) {
            if (!declarationPhase) throw error(previous(), "Declarations must follow right after START SCRIPT.");
            return varDeclaration();
        }
        declarationPhase = false;
        return statement();
    }

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

    private Stmt statement() {
        if (match(TokenType.PRINT)) return printStatement();
        if (match(TokenType.SCAN)) return scanStatement();
        return expressionStatement();
    }

    private Stmt printStatement() {
        consume(TokenType.COLON, "Expect ':' after PRINT.");
        List<Expr> expressions = new ArrayList<>();
        do {
            if (match(TokenType.DOLLAR)) expressions.add(new Expr.Literal("$"));
            else expressions.add(expression());
        } while (match(TokenType.AMPERSAND));
        return new Stmt.Print(expressions);
    }

    private Stmt scanStatement() {
        consume(TokenType.COLON, "Expect ':' after SCAN.");
        List<Token> names = new ArrayList<>();
        do {
            names.add(consume(TokenType.IDENTIFIER, "Expect variable name after SCAN."));
        } while (match(TokenType.COMMA));
        return new Stmt.Scan(names);
    }

    private Stmt expressionStatement() {
        return new Stmt.Expression(expression());
    }

    private Expr expression() {
        return assignment();
    }

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

    private Expr or() {
        Expr expr = and();
        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = comparison();
        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL, TokenType.EQUAL_EQUAL, TokenType.LESS_GREATER)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(TokenType.MINUS, TokenType.NOT)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

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

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() { return peek().type == TokenType.EOF; }

    private Token peek() { return tokens.get(current); }

    private Token previous() { return tokens.get(current - 1); }

    private LexorException error(Token token, String message) {
        return new LexorException(token.line, message);
    }
}
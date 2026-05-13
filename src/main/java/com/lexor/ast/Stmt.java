package com.lexor.ast;
import com.lexor.lexer.Token;
import java.util.List;

// Base class for all statement nodes in the LEXOR AST.
public abstract class Stmt {
    // Visitor interface for traversing the statement tree.
    public interface Visitor<R> {
        R visitExpressionStmt(Expression stmt);
        R visitPrintStmt(Print stmt);
        R visitScanStmt(Scan stmt);
        R visitDeclareStmt(Declare stmt);
    }

    // Accept method to facilitate the Visitor pattern for statements.
    public abstract <R> R accept(Visitor<R> visitor);

    // Wraps an expression that should be executed as a statement.
    public static class Expression extends Stmt {
        public final Expr expression;
        public Expression(Expr expression) { this.expression = expression; }
        @Override public <R> R accept(Visitor<R> visitor) { return visitor.visitExpressionStmt(this); }
    }

    // Represents a display command with concatenated chunks.
    public static class Print extends Stmt {
        public final List<Expr> expressions;
        public Print(List<Expr> expressions) { this.expressions = expressions; }
        @Override public <R> R accept(Visitor<R> visitor) { return visitor.visitPrintStmt(this); }
    }

    // Represents a user input command.
    public static class Scan extends Stmt {
        public final List<Token> names;
        public Scan(List<Token> names) { this.names = names; }
        @Override public <R> R accept(Visitor<R> visitor) { return visitor.visitScanStmt(this); }
    }

    // Represents a variable declaration statement with optional defaults.
    public static class Declare extends Stmt {
        public final Token type;
        public final List<Token> names;
        public final List<Expr> initializers;
        public Declare(Token type, List<Token> names, List<Expr> initializers) {
            this.type = type;
            this.names = names;
            this.initializers = initializers;
        }
        @Override public <R> R accept(Visitor<R> visitor) { return visitor.visitDeclareStmt(this); }
    }
}
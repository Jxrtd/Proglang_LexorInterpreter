package com.lexor.ast;
import com.lexor.lexer.Token;

// Base class for all expression nodes in the LEXOR AST.
public abstract class Expr {
    // Visitor interface for traversing the expression tree.
    public interface Visitor<R> {
        R visitAssignExpr(Assign expr);
        R visitBinaryExpr(Binary expr);
        R visitLiteralExpr(Literal expr);
        R visitVariableExpr(Variable expr);
        R visitUnaryExpr(Unary expr);
    }

    // Accept method to facilitate the Visitor pattern for expressions.
    public abstract <R> R accept(Visitor<R> visitor);

    // Represents a binary operation like x > 0.
    public static class Binary extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;
        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
        @Override public <R> R accept(Visitor<R> visitor) { return visitor.visitBinaryExpr(this); }
    }

    // Represents a value assignment to a variable.
    public static class Assign extends Expr {
        public final Token name;
        public final Expr value;
        public Assign(Token name, Expr value) { this.name = name; this.value = value; }
        @Override public <R> R accept(Visitor<R> visitor) { return visitor.visitAssignExpr(this); }
    }

    // Represents a fixed literal value (Integer, Double, String, Boolean).
    public static class Literal extends Expr {
        public final Object value;
        public Literal(Object value) { this.value = value; }
        @Override public <R> R accept(Visitor<R> visitor) { return visitor.visitLiteralExpr(this); }
    }

    // Represents a reference to a variable by name.
    public static class Variable extends Expr {
        public final Token name;
        public Variable(Token name) { this.name = name; }
        @Override public <R> R accept(Visitor<R> visitor) { return visitor.visitVariableExpr(this); }
    }

    // Represents a unary operation like -5.
    public static class Unary extends Expr {
        public final Token operator;
        public final Expr right;
        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }
        @Override public <R> R accept(Visitor<R> visitor) { return visitor.visitUnaryExpr(this); }
    }
}
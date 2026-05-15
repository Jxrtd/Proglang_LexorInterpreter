package com.lexor.ast;
import java.util.List;

import com.lexor.lexer.Token;

public abstract class Stmt {
    public interface Visitor<R> {
        R visitExpressionStmt(Expression stmt);
        R visitPrintStmt(Print stmt);
        R visitScanStmt(Scan stmt);
        R visitDeclareStmt(Declare stmt);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    public static class Expression extends Stmt {
        public final Expr expression;
        public Expression(Expr expression) { this.expression = expression; }
        @Override public <R> R accept(Visitor<R> visitor) { return visitor.visitExpressionStmt(this); }
    }

    public static class Print extends Stmt {
        public final List<Expr> expressions;
        public Print(List<Expr> expressions) { this.expressions = expressions; }
        @Override public <R> R accept(Visitor<R> visitor) { return visitor.visitPrintStmt(this); }
    }

    public static class Scan extends Stmt {
        public final List<Token> names;
        public Scan(List<Token> names) { this.names = names; }
        @Override public <R> R accept(Visitor<R> visitor) { return visitor.visitScanStmt(this); }
    }

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
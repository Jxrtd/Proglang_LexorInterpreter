package com.lexor.interpreter;

import java.util.List;
import java.util.Scanner;

import com.lexor.ast.Expr;
import com.lexor.ast.Stmt;
import com.lexor.core.LexorException;
import com.lexor.lexer.Token;
import com.lexor.lexer.TokenType;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private final Environment environment = new Environment();
    private final Scanner scanner = new Scanner(System.in);

    public void interpret(List<Stmt> statements) {
        for (Stmt statement : statements) execute(statement);
    }

    private void execute(Stmt stmt) { stmt.accept(this); }

    private Object evaluate(Expr expr) { return expr.accept(this); }

    @Override public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override public Void visitPrintStmt(Stmt.Print stmt) {
        for (Expr expr : stmt.expressions) {
            Object value = evaluate(expr);
            if (value != null && value.equals("$")) System.out.println();
            else if (value instanceof Boolean) System.out.print(value.toString().toUpperCase());
            else System.out.print(value);
        }
        System.out.println();
        return null;
    }

    @Override public Void visitScanStmt(Stmt.Scan stmt) {
        for (Token name : stmt.names) {
            String input = scanner.nextLine();
            String type = environment.getType(name);
            Object value;
            try {
                value = switch (type) {
                    case "INT" -> Integer.valueOf(input);

                    case "FLOAT" -> Double.valueOf(input);
                    case "BOOL" -> Boolean.valueOf(input.toLowerCase());
                    case "CHAR" -> input.length() == 1 ? input : null;
                    default -> null;
                };
                if (value == null) throw new Exception();
            } catch (Exception e) {
                throw new LexorException(name.line, "Invalid input for " + type + ": " + input);
            }
            environment.assign(name, value);
        }
        return null;
    }

    @Override public Void visitDeclareStmt(Stmt.Declare stmt) {
        for (int i = 0; i < stmt.names.size(); i++) {
            environment.define(stmt.names.get(i), stmt.type.lexeme);
            if (stmt.initializers.get(i) != null) environment.assign(stmt.names.get(i), evaluate(stmt.initializers.get(i)));
        }
        return null;
    }

    @Override public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        return switch (expr.operator.type) {
            case GREATER -> (double)toDouble(left) > (double)toDouble(right);
            case GREATER_EQUAL -> (double)toDouble(left) >= (double)toDouble(right);
            case LESS -> (double)toDouble(left) < (double)toDouble(right);
            case LESS_EQUAL -> (double)toDouble(left) <= (double)toDouble(right);
            case EQUAL_EQUAL -> left.equals(right);
            case LESS_GREATER -> !left.equals(right);
            case AND -> {
                if (!(left instanceof Boolean && right instanceof Boolean)) {
                    throw new LexorException(expr.operator.line, "Operands must be booleans for 'AND'.");
                }
                yield (Boolean) left && (Boolean) right;
            }
            case OR -> {
                if (!(left instanceof Boolean && right instanceof Boolean)) {
                    throw new LexorException(expr.operator.line, "Operands must be booleans for 'OR'.");
                }
                yield (Boolean) left || (Boolean) right;
            }
            case PLUS -> {
                if (left instanceof Integer && right instanceof Integer) yield (Integer) left + (Integer) right;
                yield toDouble(left) + toDouble(right);
            }
            case MINUS -> {
                if (left instanceof Integer && right instanceof Integer) yield (Integer) left - (Integer) right;
                yield toDouble(left) - toDouble(right);
            }
            case STAR -> {
                if (left instanceof Integer && right instanceof Integer) yield (Integer) left * (Integer) right;
                yield toDouble(left) * toDouble(right);
            }
            case SLASH -> {
                if (toDouble(right) == 0) throw new LexorException(expr.operator.line, "Division by zero.");
                if (left instanceof Integer && right instanceof Integer) yield (Integer) left / (Integer) right;
                yield toDouble(left) / toDouble(right);
            }
            case PERCENT -> {
                if (toDouble(right) == 0) throw new LexorException(expr.operator.line, "Division by zero.");
                if (left instanceof Integer && right instanceof Integer) yield (Integer) left % (Integer) right;
                yield toDouble(left) % toDouble(right);
            }
            default -> null;
        };
    }

    private double toDouble(Object o) {
        if (o instanceof Integer integer) return integer.doubleValue();
        if (o instanceof Double aDouble) return aDouble;
        throw new RuntimeException("Number expected.");
    }

    @Override public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    @Override public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        if (expr.operator.type == TokenType.MINUS) {
            if (right instanceof Integer integer) return -integer;
            if (right instanceof Double aDouble) return -aDouble;
            throw new LexorException(expr.operator.line, "Unary '-' only applies to numbers.");
        }
        if (expr.operator.type == TokenType.NOT) {
            if (!(right instanceof Boolean)) {
                throw new LexorException(expr.operator.line, "Unary 'NOT' only applies to booleans.");
            }
            return !(Boolean) right;
        }
        return null;
    }
}
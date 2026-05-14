package com.lexor.interpreter;

import java.util.List;
import java.util.Scanner;

import com.lexor.ast.Expr;
import com.lexor.ast.Stmt;
import com.lexor.core.LexorException;
import com.lexor.lexer.Token;
import com.lexor.lexer.TokenType;

// Walks through the AST nodes and performs the requested actions.
public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private final Environment environment = new Environment();
    private final Scanner scanner = new Scanner(System.in);

    // Entry point for executing the fully parsed script.
    public void interpret(List<Stmt> statements) {
        for (Stmt statement : statements) execute(statement);
    }

    // Evaluates a statement node.
    private void execute(Stmt stmt) { stmt.accept(this); }

    // Evaluates an expression node and returns its result.
    private Object evaluate(Expr expr) { return expr.accept(this); }

    // Processes a basic expression statement.
    @Override public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    // Processes the display logic, including newlines and boolean formatting.
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

    // Processes user input for one or more variables.
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

    // Defines variables in the environment based on a declaration node.
    @Override public Void visitDeclareStmt(Stmt.Declare stmt) {
        for (int i = 0; i < stmt.names.size(); i++) {
            environment.define(stmt.names.get(i), stmt.type.lexeme);
            if (stmt.initializers.get(i) != null) environment.assign(stmt.names.get(i), evaluate(stmt.initializers.get(i)));
        }
        return null;
    }

    // Performs variable assignment at runtime.
    @Override public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    // Evaluates comparison and arithmetic operations.
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

    // Resolves a literal node to its raw Java value.
    @Override public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    // Resolves a variable name to its current value in the environment.
    @Override public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    // Evaluates unary operations (e.g., -5, NOT flag).
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
package com.lexor.core;

// Evaluates expressions with operator precedence and support for arithmetic/logic.
public class ExpressionEvaluator {

    // Main entry point for evaluation.
    public Object evaluate(String expression, SymbolTable table) {
        expression = expression.trim();
        if (expression.isEmpty()) return null;
        if (isStringLiteral(expression)) return parseStringLiteral(expression);
        return eval(expression, table);
    }

    // Recursive evaluation logic with simplified precedence handling.
    private Object eval(String exp, SymbolTable table) {
        exp = exp.trim();
        if (exp.isEmpty()) return null;

        // Handle parentheses.
        if (exp.startsWith("(") && exp.endsWith(")")) {
            if (isBalanced(exp.substring(1, exp.length() - 1))) {
                return eval(exp.substring(1, exp.length() - 1), table);
            }
        }

        // Split by logical OR.
        int idx = findSplitter(exp, " OR ");
        if (idx != -1) return toBool(eval(exp.substring(0, idx), table)) || toBool(eval(exp.substring(idx + 4), table));

        // Split by logical AND.
        idx = findSplitter(exp, " AND ");
        if (idx != -1) return toBool(eval(exp.substring(0, idx), table)) && toBool(eval(exp.substring(idx + 5), table));

        // Handle comparisons.
        if ((idx = findSplitter(exp, "==")) != -1) return compare(exp, idx, 2, table) == 0;
        if ((idx = findSplitter(exp, "<>")) != -1) return compare(exp, idx, 2, table) != 0;
        if ((idx = findSplitter(exp, ">=")) != -1) return compare(exp, idx, 2, table) >= 0;
        if ((idx = findSplitter(exp, "<=")) != -1) return compare(exp, idx, 2, table) <= 0;
        if ((idx = findSplitter(exp, ">")) != -1)  return compare(exp, idx, 1, table) > 0;
        if ((idx = findSplitter(exp, "<")) != -1)  return compare(exp, idx, 1, table) < 0;

        // Handle Addition and Subtraction.
        if ((idx = findLastSplitter(exp, "+", "-")) != -1) return performArithmetic(exp, idx, table);

        // Handle multiplication, division, and modulo.
        if ((idx = findLastSplitter(exp, "*", "/", "%")) != -1) return performArithmetic(exp, idx, table);

        // Handle Unary operators.
        if (exp.startsWith("NOT ")) return !toBool(eval(exp.substring(4), table));
        if (exp.startsWith("-")) {
            Object v = eval(exp.substring(1), table);
            return (v instanceof Double) ? -(Double)v : -(Integer)v;
        }

        return resolveBaseValue(exp, table);
    }

    // Finds split point for binary operators.
    private int findSplitter(String exp, String op) {
        int balance = 0;
        for (int i = 0; i < exp.length(); i++) {
            char c = exp.charAt(i);
            if (c == '(') balance++;
            else if (c == ')') balance--;
            else if (balance == 0 && exp.startsWith(op, i)) return i;
        }
        return -1;
    }

    // Finds split point from the end for left-to-right associativity.
    private int findLastSplitter(String exp, String... ops) {
        int balance = 0;
        for (int i = exp.length() - 1; i >= 0; i--) {
            char c = exp.charAt(i);
            if (c == ')') balance++;
            else if (c == '(') balance--;
            else if (balance == 0) {
                for (String op : ops) {
                    if (exp.startsWith(op, i)) {
                        if (op.equals("-") && isUnary(exp, i)) continue;
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    // Checks if '-' is unary.
    private boolean isUnary(String exp, int pos) {
        if (pos == 0) return true;
        int p = pos - 1;
        while (p >= 0 && Character.isWhitespace(exp.charAt(p))) p--;
        if (p < 0) return true;
        return "+-*/%(=><".indexOf(exp.charAt(p)) != -1;
    }

    // Checks for balanced parentheses.
    private boolean isBalanced(String inner) {
        int b = 0;
        for (char c : inner.toCharArray()) {
            if (c == '(') b++; else if (c == ')') b--;
            if (b < 0) return false;
        }
        return b == 0;
    }

    // Executes arithmetic operations with type promotion.
    private Object performArithmetic(String exp, int idx, SymbolTable table) {
        char op = exp.charAt(idx);
        Object left = eval(exp.substring(0, idx), table);
        Object right = eval(exp.substring(idx + 1), table);
        if (!(left instanceof Number) || !(right instanceof Number)) throw new LexorException("Arithmetic error: non-numeric operands.");
        double l = ((Number)left).doubleValue();
        double r = ((Number)right).doubleValue();
        if ((op == '/' || op == '%') && r == 0) throw new LexorException("Division by zero.");
        boolean isFloat = (left instanceof Double || right instanceof Double || op == '/');
        switch (op) {
            case '+': return isFloat ? l + r : (int)l + (int)r;
            case '-': return isFloat ? l - r : (int)l - (int)r;
            case '*': return isFloat ? l * r : (int)l * (int)r;
            case '/': return l / r;
            case '%': return isFloat ? l % r : (int)l % (int)r;
        }
        return null;
    }

    // Compares two evaluated values.
    private int compare(String exp, int idx, int opLen, SymbolTable table) {
        Object l = eval(exp.substring(0, idx), table);
        Object r = eval(exp.substring(idx + opLen), table);
        if (l instanceof Number && r instanceof Number) return Double.compare(((Number)l).doubleValue(), ((Number)r).doubleValue());
        return String.valueOf(l).compareTo(String.valueOf(r));
    }

    // Resolves literals or variable names.
    private Object resolveBaseValue(String token, SymbolTable table) {
        token = token.trim();
        if (table.contains(token)) return table.get(token);
        if (token.equals("TRUE")) return true;
        if (token.equals("FALSE")) return false;
        try {
            if (token.contains(".")) return Double.parseDouble(token);
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            if (token.matches("^[a-zA-Z_].*")) throw new LexorException("Undefined variable: " + token);
            return token;
        }
    }

    // Helper for boolean conversion.
    private boolean toBool(Object o) {
        if (o instanceof Boolean) return (Boolean) o;
        String s = String.valueOf(o);
        if (s.equals("TRUE")) return true;
        if (s.equals("FALSE")) return false;
        return Boolean.parseBoolean(s);
    }

    // Checks for string literal markers.
    private boolean isStringLiteral(String s) {
        return (s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"));
    }

    // Parses a string literal and converts to boolean if applicable (strictly capitalized).
    private Object parseStringLiteral(String s) {
        String inner = s.substring(1, s.length() - 1);
        if (inner.equals("TRUE")) return true;
        if (inner.equals("FALSE")) return false;
        return inner;
    }
}
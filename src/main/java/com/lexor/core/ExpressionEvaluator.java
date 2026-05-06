package com.lexor.core;

/**
 * Evaluates expressions with operator precedence.
 */
public class ExpressionEvaluator {

    public Object evaluate(String expression, SymbolTable table) {
        expression = expression.trim();
        if (expression.isEmpty()) return null;

        // 1. Literal Strings
        if (isStringLiteral(expression)) {
            return parseStringLiteral(expression);
        }

        return eval(expression, table);
    }

    private Object eval(String exp, SymbolTable table) {
        exp = exp.trim();
        if (exp.isEmpty()) return null;

        // Parentheses handling
        if (exp.startsWith("(") && exp.endsWith(")")) {
            if (isBalanced(exp.substring(1, exp.length() - 1))) {
                return eval(exp.substring(1, exp.length() - 1), table);
            }
        }

        // Logical OR
        int idx = findSplitter(exp, " OR ");
        if (idx != -1) return toBool(eval(exp.substring(0, idx), table)) || toBool(eval(exp.substring(idx + 4), table));

        // Logical AND
        idx = findSplitter(exp, " AND ");
        if (idx != -1) return toBool(eval(exp.substring(0, idx), table)) && toBool(eval(exp.substring(idx + 5), table));

        // Comparisons
        if ((idx = findSplitter(exp, "==")) != -1) return compare(exp, idx, 2, table) == 0;
        if ((idx = findSplitter(exp, "<>")) != -1) return compare(exp, idx, 2, table) != 0;
        if ((idx = findSplitter(exp, ">=")) != -1) return compare(exp, idx, 2, table) >= 0;
        if ((idx = findSplitter(exp, "<=")) != -1) return compare(exp, idx, 2, table) <= 0;
        if ((idx = findSplitter(exp, ">")) != -1)  return compare(exp, idx, 1, table) > 0;
        if ((idx = findSplitter(exp, "<")) != -1)  return compare(exp, idx, 1, table) < 0;

        // Add / Sub
        if ((idx = findLastSplitter(exp, "+", "-")) != -1) {
            return performArithmetic(exp, idx, table);
        }

        // Mul / Div / Mod
        if ((idx = findLastSplitter(exp, "*", "/", "%")) != -1) {
            return performArithmetic(exp, idx, table);
        }

        // Unary
        if (exp.startsWith("NOT ")) return !toBool(eval(exp.substring(4), table));
        if (exp.startsWith("-")) {
            Object v = eval(exp.substring(1), table);
            return (v instanceof Double) ? -(Double)v : -(Integer)v;
        }

        return resolveBaseValue(exp, table);
    }

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

    private boolean isUnary(String exp, int pos) {
        if (pos == 0) return true;
        int p = pos - 1;
        while (p >= 0 && Character.isWhitespace(exp.charAt(p))) p--;
        if (p < 0) return true;
        char prev = exp.charAt(p);
        return "+-*/%(=><".indexOf(prev) != -1;
    }

    private boolean isBalanced(String inner) {
        int b = 0;
        for (char c : inner.toCharArray()) {
            if (c == '(') b++; else if (c == ')') b--;
            if (b < 0) return false;
        }
        return b == 0;
    }

    private Object performArithmetic(String exp, int idx, SymbolTable table) {
        char op = exp.charAt(idx);
        Object left = eval(exp.substring(0, idx), table);
        Object right = eval(exp.substring(idx + 1), table);
        
        if (!(left instanceof Number) || !(right instanceof Number)) 
            throw new RuntimeException("Arithmetic error: non-numeric operands.");

        double l = ((Number)left).doubleValue();
        double r = ((Number)right).doubleValue();
        
        if ((op == '/' || op == '%') && r == 0) throw new RuntimeException("Division by zero.");

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

    private int compare(String exp, int idx, int opLen, SymbolTable table) {
        Object l = eval(exp.substring(0, idx), table);
        Object r = eval(exp.substring(idx + opLen), table);
        if (l instanceof Number && r instanceof Number) 
            return Double.compare(((Number)l).doubleValue(), ((Number)r).doubleValue());
        return String.valueOf(l).compareTo(String.valueOf(r));
    }

    private Object resolveBaseValue(String token, SymbolTable table) {
        token = token.trim();
        if (table.contains(token)) return table.get(token);
        if (token.equalsIgnoreCase("TRUE")) return true;
        if (token.equalsIgnoreCase("FALSE")) return false;
        try {
            if (token.contains(".")) return Double.parseDouble(token);
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            if (token.matches("^[a-zA-Z_].*")) throw new RuntimeException("Undefined variable: " + token);
            return token;
        }
    }

    private boolean toBool(Object o) {
        return (o instanceof Boolean) ? (Boolean)o : Boolean.parseBoolean(String.valueOf(o));
    }

    private boolean isStringLiteral(String s) {
        return (s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"));
    }

    private Object parseStringLiteral(String s) {
        String inner = s.substring(1, s.length() - 1);
        if (inner.equalsIgnoreCase("TRUE")) return true;
        if (inner.equalsIgnoreCase("FALSE")) return false;
        return inner;
    }
}
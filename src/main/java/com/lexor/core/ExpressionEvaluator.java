package com.lexor.core;

public class ExpressionEvaluator {

    public Object evaluate(String expression, SymbolTable table) {
        expression = expression.trim();
        if (expression.isEmpty()) return null;

        // Handle string literals directly, but check for booleans inside
        if ((expression.startsWith("\"") && expression.endsWith("\"")) || (expression.startsWith("'") && expression.endsWith("'"))) {
            String content = expression.substring(1, expression.length() - 1);
            if (content.equalsIgnoreCase("TRUE")) return true;
            if (content.equalsIgnoreCase("FALSE")) return false;
            return content;
        }

        // Tokenize and use Shunting-yard or similar for precedence
        // For simplicity and since LEXOR is likely to have simple expressions, 
        // let's use a simplified recursive approach or a basic parser.
        
        return eval(expression, table);
    }

    private Object eval(String exp, SymbolTable table) {
        exp = exp.trim();
        
        // Remove outer parenthesis if they wrap the whole expression
        if (exp.startsWith("(") && exp.endsWith(")")) {
            int balance = 0;
            boolean wrapAll = true;
            for (int i = 0; i < exp.length(); i++) {
                if (exp.charAt(i) == '(') balance++;
                else if (exp.charAt(i) == ')') balance--;
                if (balance == 0 && i < exp.length() - 1) {
                    wrapAll = false;
                    break;
                }
            }
            if (wrapAll) return eval(exp.substring(1, exp.length() - 1), table);
        }

        // Split by operators in reverse precedence
        
        // 1. Logical OR
        int opIdx = findOperator(exp, "OR");
        if (opIdx != -1) return toBool(eval(exp.substring(0, opIdx), table)) || toBool(eval(exp.substring(opIdx + 2), table));

        // 2. Logical AND
        opIdx = findOperator(exp, "AND");
        if (opIdx != -1) return toBool(eval(exp.substring(0, opIdx), table)) && toBool(eval(exp.substring(opIdx + 3), table));

        // 3. Comparisons
        opIdx = findOperator(exp, "==");
        if (opIdx != -1) return compare(eval(exp.substring(0, opIdx), table), eval(exp.substring(opIdx + 2), table)) == 0;
        opIdx = findOperator(exp, "<>");
        if (opIdx != -1) return compare(eval(exp.substring(0, opIdx), table), eval(exp.substring(opIdx + 2), table)) != 0;
        opIdx = findOperator(exp, ">=");
        if (opIdx != -1) return compare(eval(exp.substring(0, opIdx), table), eval(exp.substring(opIdx + 2), table)) >= 0;
        opIdx = findOperator(exp, "<=");
        if (opIdx != -1) return compare(eval(exp.substring(0, opIdx), table), eval(exp.substring(opIdx + 2), table)) <= 0;
        opIdx = findOperator(exp, ">");
        if (opIdx != -1) return compare(eval(exp.substring(0, opIdx), table), eval(exp.substring(opIdx + 1), table)) > 0;
        opIdx = findOperator(exp, "<");
        if (opIdx != -1) return compare(eval(exp.substring(0, opIdx), table), eval(exp.substring(opIdx + 1), table)) < 0;

        // 4. Add/Sub (last match to handle left-to-right)
        opIdx = findOperatorLast(exp, "+", "-");
        if (opIdx != -1) {
            char op = exp.charAt(opIdx);
            Object left = eval(exp.substring(0, opIdx), table);
            Object right = eval(exp.substring(opIdx + 1), table);
            return arithmetic(left, right, op);
        }

        // 5. Mul/Div/Mod
        opIdx = findOperatorLast(exp, "*", "/", "%");
        if (opIdx != -1) {
            char op = exp.charAt(opIdx);
            Object left = eval(exp.substring(0, opIdx), table);
            Object right = eval(exp.substring(opIdx + 1), table);
            return arithmetic(left, right, op);
        }

        // 6. Unary
        if (exp.startsWith("NOT")) return !toBool(eval(exp.substring(3), table));
        if (exp.startsWith("-")) {
            Object val = eval(exp.substring(1), table);
            if (val instanceof Double) return -(Double) val;
            return -(Integer) val;
        }
        if (exp.startsWith("+")) return eval(exp.substring(1), table);

        // 7. Base case: Literal or Variable
        return getRawValue(exp, table);
    }

    private int findOperator(String exp, String op) {
        int balance = 0;
        for (int i = 0; i < exp.length(); i++) {
            char c = exp.charAt(i);
            if (c == '(') balance++;
            else if (c == ')') balance--;
            else if (balance == 0) {
                if (exp.startsWith(op, i)) {
                    // Check if it's a standalone word for AND/OR
                    if (op.equals("AND") || op.equals("OR")) {
                        boolean before = (i == 0 || Character.isWhitespace(exp.charAt(i - 1)));
                        boolean after = (i + op.length() == exp.length() || Character.isWhitespace(exp.charAt(i + op.length())));
                        if (before && after) return i;
                    } else {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private int findOperatorLast(String exp, String... ops) {
        int balance = 0;
        for (int i = exp.length() - 1; i >= 0; i--) {
            char c = exp.charAt(i);
            if (c == ')') balance++;
            else if (c == '(') balance--;
            else if (balance == 0) {
                for (String op : ops) {
                    if (exp.startsWith(op, i)) {
                        // Avoid confusion with negative numbers: '-' is an operator only if preceded by a value-like token
                        if (op.equals("-")) {
                            if (i == 0) continue; // Unary at start
                            
                            // Check previous non-whitespace character
                            int prevIdx = i - 1;
                            while (prevIdx >= 0 && Character.isWhitespace(exp.charAt(prevIdx))) {
                                prevIdx--;
                            }
                            if (prevIdx < 0) continue; // Unary after spaces
                            
                            char prev = exp.charAt(prevIdx);
                            if (prev == '+' || prev == '-' || prev == '*' || prev == '/' || prev == '%' || prev == '(' || prev == '<' || prev == '>' || prev == '=') continue;
                        }
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private Object arithmetic(Object left, Object right, char op) {
        if (left instanceof Double || right instanceof Double) {
            double l = ((Number) left).doubleValue();
            double r = ((Number) right).doubleValue();
            switch (op) {
                case '+': return l + r;
                case '-': return l - r;
                case '*': return l * r;
                case '/': return l / r;
                case '%': return l % r;
            }
        } else {
            int l = ((Number) left).intValue();
            int r = ((Number) right).intValue();
            switch (op) {
                case '+': return l + r;
                case '-': return l - r;
                case '*': return l * r;
                case '/': return l / r;
                case '%': return l % r;
            }
        }
        return null;
    }

    private int compare(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            return Double.compare(((Number) left).doubleValue(), ((Number) right).doubleValue());
        }
        if (left instanceof Boolean && right instanceof Boolean) {
            return ((Boolean) left).compareTo((Boolean) right);
        }
        if (left instanceof String && right instanceof String) {
            return ((String) left).compareTo((String) right);
        }
        return String.valueOf(left).compareTo(String.valueOf(right));
    }

    private boolean toBool(Object obj) {
        if (obj instanceof Boolean) return (Boolean) obj;
        return Boolean.parseBoolean(String.valueOf(obj));
    }

    private Object getRawValue(String token, SymbolTable table) {
        token = token.trim();
        if (table.contains(token)) return table.get(token);
        
        if (token.equalsIgnoreCase("TRUE") || token.equals("\"TRUE\"") || token.equals("'TRUE'")) return true;
        if (token.equalsIgnoreCase("FALSE") || token.equals("\"FALSE\"") || token.equals("'FALSE'")) return false;
        
        if (token.startsWith("'") || token.startsWith("\"")) {
            return token.substring(1, token.length() - 1);
        }

        try {
            if (token.contains(".")) return Double.parseDouble(token);
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            return token;
        }
    }
}
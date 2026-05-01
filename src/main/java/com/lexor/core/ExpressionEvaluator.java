package com.lexor.core;

public class ExpressionEvaluator {

    public Object evaluate(String expression, SymbolTable table) {
        expression = expression.trim();

        // 1. Unary Operators (NOT, -)
        if (expression.startsWith("NOT ")) {
            Object val = evaluate(expression.substring(4).trim(), table);
            if (val instanceof Boolean) {
            return !(boolean) val;
            } else {
        // If it's not a boolean, let's try to parse it or default to false
            return !Boolean.parseBoolean(val.toString());
            }
        }      
        
        // Handle negative numbers like -5
        if (expression.startsWith("-") && expression.substring(1).trim().matches("\\d+")) {
            return -Integer.parseInt(expression.substring(1).trim());
        }

        // 2. Logical Operations (AND, OR)
        if (expression.contains(" AND ")) {
            String[] parts = expression.split(" AND ", 2);
            return (boolean) evaluate(parts[0], table) && (boolean) evaluate(parts[1], table);
        }
        if (expression.contains(" OR ")) {
            String[] parts = expression.split(" OR ", 2);
            return (boolean) evaluate(parts[0], table) || (boolean) evaluate(parts[1], table);
        }

        // 3. Arithmetic Operations (+, -, *, /)
        if (expression.contains("+")) {
            String[] parts = expression.split("\\+", 2);
            return toInt(evaluate(parts[0], table)) + toInt(evaluate(parts[1], table));
        }
        if (expression.contains("-") && !expression.startsWith("-")) {
            String[] parts = expression.split("-", 2);
            return toInt(evaluate(parts[0], table)) - toInt(evaluate(parts[1], table));
        }

        // 4. Base Case: Literal or Variable
        return getRawValue(expression, table);
    }

    private Object getRawValue(String token, SymbolTable table) {
        token = token.trim();
        // Check if it's a variable in the table
        if (table.contains(token)) return table.get(token);
        
        // Check if it's a Boolean literal
        if (token.equalsIgnoreCase("TRUE")) return true;
        if (token.equalsIgnoreCase("FALSE")) return false;
        
        // Check if it's a String literal
        if (token.startsWith("'") || token.startsWith("\"")) {
            return token.substring(1, token.length() - 1);
        }

        // Otherwise assume it's an Integer
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            return token; // Return as string if all else fails
        }
    }

    private int toInt(Object obj) {
        if (obj instanceof Integer) return (int) obj;
        return Integer.parseInt(obj.toString());
    }
}
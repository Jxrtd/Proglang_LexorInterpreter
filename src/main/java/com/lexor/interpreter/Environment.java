package com.lexor.interpreter;

import java.util.HashMap;
import java.util.Map;

import com.lexor.core.LexorException;
import com.lexor.lexer.Token;

public class Environment {
    private static class Variable {
        Object value;
        final String type;
        Variable(Object value, String type) { this.value = value; this.type = type; }
    }

    private final Map<String, Variable> values = new HashMap<>();

    public void define(Token name, String type) {
        if (values.containsKey(name.lexeme)) throw new LexorException(name.line, "Variable '" + name.lexeme + "' already declared.");
        
        Object defaultValue = switch (type.toUpperCase()) {
            case "INT" -> 0;
            case "FLOAT" -> 0.0;
            case "BOOL" -> false;
            default -> null;
        };
        
        values.put(name.lexeme, new Variable(defaultValue, type.toUpperCase()));
    }

    public void assign(Token name, Object value) {
        if (!values.containsKey(name.lexeme)) throw new LexorException(name.line, "Variable '" + name.lexeme + "' not declared.");
        Variable var = values.get(name.lexeme);
        validateType(name, var.type, value);
        
        if (var.type.equals("BOOL") && value instanceof String s) {
            value = s.equals("TRUE");
        }
        
        var.value = value;
    }

    public Object get(Token name) {
        if (!values.containsKey(name.lexeme)) throw new LexorException(name.line, "Undefined variable '" + name.lexeme + "'.");
        return values.get(name.lexeme).value;
    }

    public String getType(Token name) {
        if (!values.containsKey(name.lexeme)) throw new LexorException(name.line, "Undefined variable '" + name.lexeme + "'.");
        return values.get(name.lexeme).type;
    }

    private void validateType(Token name, String expectedType, Object value) {
        boolean ok = false;
        switch (expectedType) {
            case "INT" -> ok = value instanceof Integer;
            case "FLOAT" -> ok = value instanceof Double;
            case "BOOL" -> {
                if (value instanceof Boolean) ok = true;
                else if (value instanceof String s) {
                    ok = s.equals("TRUE") || s.equals("FALSE");
                }
            }
            case "CHAR" -> ok = value instanceof String && ((String) value).length() == 1;
        }
        if (!ok) throw new LexorException(name.line, "Type Mismatch: '" + name.lexeme + "' is " + expectedType + " but assigned " + value.getClass().getSimpleName());
    }
}
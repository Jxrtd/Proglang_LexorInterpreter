package com.lexor.interpreter;

import com.lexor.core.LexorException;
import com.lexor.lexer.Token;
import java.util.HashMap;
import java.util.Map;

// Manages variable storage, state, and strict data type enforcement.
public class Environment {
    private static class Variable {
        Object value;
        final String type;
        Variable(Object value, String type) { this.value = value; this.type = type; }
    }

    private final Map<String, Variable> values = new HashMap<>();

    // Reserves a slot for a new variable with a specific type.
    public void define(Token name, String type) {
        if (values.containsKey(name.lexeme)) throw new LexorException(name.line, "Variable '" + name.lexeme + "' already declared.");
        values.put(name.lexeme, new Variable(null, type.toUpperCase()));
    }

    // Updates a variable's value, strictly checking type compatibility.
    public void assign(Token name, Object value) {
        if (!values.containsKey(name.lexeme)) throw new LexorException(name.line, "Variable '" + name.lexeme + "' not declared.");
        Variable var = values.get(name.lexeme);
        validateType(name, var.type, value);
        
        // Coerce string literals to boolean if target is BOOL
        if (var.type.equals("BOOL") && value instanceof String s) {
            value = s.equals("TRUE");
        }
        
        var.value = value;
    }

    // Retrieves a variable's current value from memory.
    public Object get(Token name) {
        if (!values.containsKey(name.lexeme)) throw new LexorException(name.line, "Undefined variable '" + name.lexeme + "'.");
        Object value = values.get(name.lexeme).value;
        if (value == null) throw new LexorException(name.line, "Variable '" + name.lexeme + "' used before assignment.");
        return value;
    }

    // Retrieves the expected type of a variable.
    public String getType(Token name) {
        if (!values.containsKey(name.lexeme)) throw new LexorException(name.line, "Undefined variable '" + name.lexeme + "'.");
        return values.get(name.lexeme).type;
    }

    // Enforces the strongly-typed nature of the LEXOR language.
    private void validateType(Token name, String expectedType, Object value) {
        boolean ok = false;
        switch (expectedType) {
            case "INT" -> ok = value instanceof Integer;
            case "FLOAT" -> ok = value instanceof Double || value instanceof Integer;
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
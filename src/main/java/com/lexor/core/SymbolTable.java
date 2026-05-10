package com.lexor.core;

import java.util.HashMap;
import java.util.Map;

// Manages variable storage, retrieval, and type tracking.
public class SymbolTable {
    // Internal representation of a variable, storing its value and type.
    private static class Variable {
        Object value;
        String type;
        Variable(Object value, String type) {
            this.value = value;
            this.type = type;
        }
    }

    private final Map<String, Variable> variables = new HashMap<>();

    // Updates the value of an existing variable.
    public void set(String name, Object value) {
        Variable var = variables.get(name);
        if (var == null) throw new LexorException("Error: Variable '" + name + "' not declared.");
        var.value = value;
    }

    // Retrieves the value of a variable.
    public Object get(String name) {
        Variable var = variables.get(name);
        return var != null ? var.value : null;
    }

    // Retrieves the declared type of a variable.
    public String getType(String name) {
        Variable var = variables.get(name);
        return var != null ? var.type : null;
    }

    // Checks if a variable has been declared.
    public boolean contains(String name) {
        return variables.containsKey(name);
    }

    // Declares a new variable with a specific type.
    public void declare(String name, String type) {
        if (this.variables.containsKey(name)) throw new LexorException("Error: Variable '" + name + "' already declared.");
        this.variables.put(name, new Variable(null, type.toUpperCase()));
    }
}
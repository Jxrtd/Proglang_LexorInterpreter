package com.lexor.core;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private static class Variable {
        Object value;
        String type;

        Variable(Object value, String type) {
            this.value = value;
            this.type = type;
        }
    }

    private final Map<String, Variable> variables = new HashMap<>();

    public void set(String name, Object value) {
        Variable var = variables.get(name);
        if (var == null) {
            throw new RuntimeException("Error: Variable '" + name + "' must be declared before assignment.");
        }
        var.value = value;
    }

    public Object get(String name) {
        Variable var = variables.get(name);
        return var != null ? var.value : null;
    }

    public String getType(String name) {
        Variable var = variables.get(name);
        return var != null ? var.type : null;
    }

    public boolean contains(String name) {
        return variables.containsKey(name);
    }

    public void declare(String name, String type) {
        if (this.variables.containsKey(name)) {
            throw new RuntimeException("Error: Variable '" + name + "' is already declared.");
        }
        this.variables.put(name, new Variable(null, type.toUpperCase()));
    }
}
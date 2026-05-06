package com.lexor.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages variable storage, retrieval, and type tracking for the LEXOR interpreter.
 */
public class SymbolTable {
    /**
     * Internal representation of a variable, storing its value and declared type.
     */
    private static class Variable {
        Object value;
        String type;

        Variable(Object value, String type) {
            this.value = value;
            this.type = type;
        }
    }

    private final Map<String, Variable> variables = new HashMap<>();

    /**
     * Updates the value of an existing variable.
     * @param name The variable name.
     * @param value The new value.
     * @throws RuntimeException if the variable has not been declared.
     */
    public void set(String name, Object value) {
        Variable var = variables.get(name);
        if (var == null) {
            throw new RuntimeException("Error: Variable '" + name + "' must be declared before assignment.");
        }
        var.value = value;
    }

    /**
     * Retrieves the value of a variable.
     * @param name The variable name.
     * @return The Object value, or null if not found.
     */
    public Object get(String name) {
        Variable var = variables.get(name);
        return var != null ? var.value : null;
    }

    /**
     * Retrieves the declared type of a variable.
     * @param name The variable name.
     * @return The type string (e.g., "INT", "FLOAT", "BOOL", "CHAR"), or null if not found.
     */
    public String getType(String name) {
        Variable var = variables.get(name);
        return var != null ? var.type : null;
    }

    /**
     * Checks if a variable has been declared.
     * @param name The variable name.
     * @return true if it exists in the symbol table.
     */
    public boolean contains(String name) {
        return variables.containsKey(name);
    }

    /**
     * Declares a new variable with a specific type.
     * @param name The variable name.
     * @param type The LEXOR data type.
     * @throws RuntimeException if the variable name is already in use.
     */
    public void declare(String name, String type) {
        if (this.variables.containsKey(name)) {
            throw new RuntimeException("Error: Variable '" + name + "' is already declared.");
        }
        this.variables.put(name, new Variable(null, type.toUpperCase()));
    }
}
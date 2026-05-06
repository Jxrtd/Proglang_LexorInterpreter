package com.lexor.core;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Object> variables = new HashMap<>();

    public void set(String name, Object value) {
        variables.put(name, value);
    }

    public Object get(String name) {
        return variables.get(name);
    }

    public boolean contains(String name) {
        return variables.containsKey(name);
    }

    
    public void declare(String name, String type) {
    if (this.variables.containsKey(name)) {
        throw new RuntimeException("Error: Variable '" + name + "' is already declared.");
    }
    this.variables.put(name, null); 
}
}
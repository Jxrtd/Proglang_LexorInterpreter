package com.lexor.handlers;

import com.lexor.core.ExpressionEvaluator;
import com.lexor.core.LexorException;
import com.lexor.core.LexorInterpreter;
import com.lexor.core.SymbolTable;
import com.lexor.core.TypeSystem;

// Handles the DECLARE command for variable creation.
public class DeclarationHandler implements CommandHandler {
    private final ExpressionEvaluator evaluator;
    public DeclarationHandler(ExpressionEvaluator evaluator) { this.evaluator = evaluator; }

    // Legacy handler placeholder.
    @Override public void handle(String[] tokens, SymbolTable symbolTable) {}

    // Processes variable declarations with optional initialization.
    @Override public void handle(String line, String[] tokens, SymbolTable symbolTable) {
        if (tokens.length < 2) throw new LexorException("Error: DECLARE requires a type.");
        String type = tokens[1].toUpperCase();
        String content = line.substring(line.toUpperCase().indexOf(type) + type.length()).trim();
        for (String dec : content.split(",")) {
            String trimmed = dec.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.contains("=")) {
                String[] parts = trimmed.split("=", 2);
                String name = parts[0].trim();
                LexorInterpreter.validateVariableName(name);
                symbolTable.declare(name, type);
                Object val = evaluator.evaluate(parts[1].trim(), symbolTable);
                if (!TypeSystem.isCompatible(type, val)) throw new LexorException("Type Mismatch: " + name + " is " + type + " but got " + val.getClass().getSimpleName());
                symbolTable.set(name, val);
            } else {
                LexorInterpreter.validateVariableName(trimmed);
                symbolTable.declare(trimmed, type);
            }
        }
    }
}
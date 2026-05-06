package com.lexor.handlers;

import com.lexor.core.ExpressionEvaluator;
import com.lexor.core.LexorInterpreter;
import com.lexor.core.SymbolTable;

/**
 * Handles the DECLARE command for creating variables with optional initial values.
 * Enforces strong typing and naming conventions.
 */
public class DeclarationHandler implements CommandHandler {
    private final ExpressionEvaluator evaluator;

    public DeclarationHandler(ExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public void handle(String[] tokens, SymbolTable symbolTable) {
        // Handled by the full-line version for better comma/space handling
    }

    /**
     * Processes the DECLARE command using the full line for accurate parsing.
     * @param line The original statement line.
     * @param tokens Pre-split tokens.
     * @param symbolTable The project's symbol table.
     */
    @Override
    public void handle(String line, String[] tokens, SymbolTable symbolTable) {
        // Format: DECLARE <TYPE> <var>[=<expr>][, <var>[=<expr>]]*
        if (tokens.length < 2) {
            throw new RuntimeException("Error: Invalid DECLARE statement. Expected type.");
        }
        String type = tokens[1].toUpperCase();
        
        // Extract content after "DECLARE <TYPE>"
        int typeIdx = line.toUpperCase().indexOf(type);
        String content = line.substring(typeIdx + type.length()).trim();
        
        // Split by commas to handle multiple declarations in one line
        String[] declarations = content.split(",");

        for (String dec : declarations) {
            String trimmedDec = dec.trim();
            if (trimmedDec.isEmpty()) continue;

            if (trimmedDec.contains("=")) {
                String[] parts = trimmedDec.split("=", 2);
                String name = parts[0].trim();
                String rawValue = parts[1].trim();

                LexorInterpreter.validateVariableName(name);
                symbolTable.declare(name, type);
                
                // Evaluate and validate the initial value
                Object value = evaluator.evaluate(rawValue, symbolTable);
                validateAndSet(symbolTable, name, type, value);
            } else {
                String name = trimmedDec;
                LexorInterpreter.validateVariableName(name);
                symbolTable.declare(name, type);
            }
        }
    }

    /**
     * Validates that the evaluated value matches the declared type.
     */
    private void validateAndSet(SymbolTable symbolTable, String name, String type, Object value) {
        if (type.equals("INT")) {
            if (!(value instanceof Integer)) {
                throw new RuntimeException("Type Mismatch: Variable '" + name + "' is INT but assigned " + (value != null ? value.getClass().getSimpleName() : "null"));
            }
        } else if (type.equals("FLOAT")) {
            if (!(value instanceof Double || value instanceof Integer)) {
                throw new RuntimeException("Type Mismatch: Variable '" + name + "' is FLOAT but assigned " + (value != null ? value.getClass().getSimpleName() : "null"));
            }
        } else if (type.equals("BOOL")) {
            if (!(value instanceof Boolean)) {
                throw new RuntimeException("Type Mismatch: Variable '" + name + "' is BOOL but assigned " + (value != null ? value.getClass().getSimpleName() : "null"));
            }
        } else if (type.equals("CHAR")) {
            if (!(value instanceof String && ((String) value).length() == 1)) {
                throw new RuntimeException("Type Mismatch: Variable '" + name + "' is CHAR but assigned " + (value != null ? value.getClass().getSimpleName() : "null"));
            }
        }
        symbolTable.set(name, value);
    }
}
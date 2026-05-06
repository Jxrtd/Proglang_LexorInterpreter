package com.lexor.handlers;

import com.lexor.core.ExpressionEvaluator;
import com.lexor.core.LexorInterpreter;
import com.lexor.core.SymbolTable;

public class DeclarationHandler implements CommandHandler {
    private final ExpressionEvaluator evaluator;

    public DeclarationHandler(ExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public void handle(String[] tokens, SymbolTable symbolTable) {
        // Format: DECLARE INT a=1, b=2.3
        if (tokens.length < 2) {
            throw new RuntimeException("Error: Invalid DECLARE statement. Expected type.");
        }
        String type = tokens[1].toUpperCase();
        
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < tokens.length; i++) {
            sb.append(tokens[i]);
        }

        String content = sb.toString();
        // Split by commas, but be careful not to split inside quotes if any (though LEXOR seems simple)
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
                
                Object value = evaluator.evaluate(rawValue, symbolTable);
                validateAndSet(symbolTable, name, type, value);
            } else {
                String name = trimmedDec;
                LexorInterpreter.validateVariableName(name);
                symbolTable.declare(name, type);
            }
        }
    }

    private void validateAndSet(SymbolTable symbolTable, String name, String type, Object value) {
        if (type.equals("INT")) {
            if (!(value instanceof Integer)) {
                throw new RuntimeException("Type Mismatch: Variable '" + name + "' is INT but assigned " + value.getClass().getSimpleName());
            }
        } else if (type.equals("FLOAT")) {
            if (!(value instanceof Double || value instanceof Integer)) {
                throw new RuntimeException("Type Mismatch: Variable '" + name + "' is FLOAT but assigned " + value.getClass().getSimpleName());
            }
        } else if (type.equals("BOOL")) {
            if (!(value instanceof Boolean)) {
                throw new RuntimeException("Type Mismatch: Variable '" + name + "' is BOOL but assigned " + value.getClass().getSimpleName());
            }
        } else if (type.equals("CHAR")) {
            if (!(value instanceof String && ((String) value).length() == 1)) {
                throw new RuntimeException("Type Mismatch: Variable '" + name + "' is CHAR but assigned " + value.getClass().getSimpleName());
            }
        }
        symbolTable.set(name, value);
    }
}
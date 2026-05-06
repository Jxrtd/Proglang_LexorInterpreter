package com.lexor.handlers;

import com.lexor.core.SymbolTable;

public class DeclarationHandler implements CommandHandler {
    @Override
    public void handle(String[] tokens, SymbolTable symbolTable) {
        // Format: DECLARE INT a=1,b=2.3
        String type = tokens[1].toUpperCase();
        
        // 1. Reconstruct the rest of the line (omitting 'DECLARE' and 'INT')
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < tokens.length; i++) {
            sb.append(tokens[i]);
        }

        // Clean up internal spacing
        String sanitized = sb.toString().replace(" ", "");
        
        // Split strictly by commas
        String[] declarations = sanitized.split(",");

        for (String dec : declarations) {
            String trimmedDec = dec.trim();
            if (trimmedDec.isEmpty()) continue;

            if (trimmedDec.contains("=")) {
                String[] parts = trimmedDec.split("=");
                String name = parts[0].trim();
                String value = parts[1].trim().replace("\"", "").replace("'", "");

                if (name.isEmpty()) continue;

                // --- STRICT TYPE CHECKING ---
                if (type.equals("INT") && value.contains(".")) {
                    throw new RuntimeException("Runtime Error: Type Mismatch. Variable '" + name + "' is INT but assigned decimal value '" + value + "'.");
                }

                symbolTable.declare(name, type);
                
                // Store with correct types
                if (type.equals("INT")) {
                    symbolTable.set(name, Integer.parseInt(value));
                } else if (type.equals("FLOAT")) {
                    symbolTable.set(name, Double.parseDouble(value));
                } else {
                    symbolTable.set(name, value);
                }
            } else {
                String name = trimmedDec;
                if (!name.isEmpty()) {
                    symbolTable.declare(name, type);
                }
            }
        }
    }
}
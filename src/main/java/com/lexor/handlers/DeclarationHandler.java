package com.lexor.handlers;

import com.lexor.core.SymbolTable;

public class DeclarationHandler implements CommandHandler {
    @Override
    public void handle(String[] tokens, SymbolTable symbolTable) {
        // Format: DECLARE INT a=1, b=2.3
        String type = tokens[1].toUpperCase();
        
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < tokens.length; i++) sb.append(tokens[i]);
        
        String[] declarations = sb.toString().split(",");
        
        for (String dec : declarations) {
            if (dec.contains("=")) {
                String[] parts = dec.split("=");
                String name = parts[0].trim();
                String value = parts[1].trim().replace("\"", "").replace("'", "");
                
                if (type.equals("INT") && value.contains(".")) {
                    throw new RuntimeException("Error: Type Mismatch. Variable '" + name + "' is INT but assigned decimal value '" + value + "'.");
                }
                
                symbolTable.declare(name, type);
                
                if (type.equals("INT")) {
                    symbolTable.set(name, Integer.parseInt(value));
                } else if (type.equals("FLOAT")) {
                    symbolTable.set(name, Double.parseDouble(value));
                } else {
                    symbolTable.set(name, value);
                }
            } else {
                symbolTable.declare(dec.trim(), type);
            }
        }
    }
}
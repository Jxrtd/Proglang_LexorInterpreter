package com.lexor.handlers;

import com.lexor.core.SymbolTable;

public class AssignmentHandler implements CommandHandler {
    @Override
    public void handle(String[] tokens, SymbolTable symbolTable) {
        String line = String.join("", tokens);
        String[] parts = line.split("=");
        
        String valueStr = parts[parts.length - 1].replace("'", "").replace("\"", "");
        
        for (int i = 0; i < parts.length - 1; i++) {
            symbolTable.set(parts[i].trim(), valueStr);
        }
    }
}
package com.lexor.handlers;

import com.lexor.core.SymbolTable;

public class DeclarationHandler implements CommandHandler {
    @Override
    public void handle(String[] tokens, SymbolTable symbolTable) {
        // Format: DECLARE INT x, y, z
        String type = tokens[1];
        
        // Join remaining tokens and split by comma to handle multiple variables
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < tokens.length; i++) sb.append(tokens[i]);
        
        String[] vars = sb.toString().split(",");
        for (String varName : vars) {
            symbolTable.declare(varName.trim(), type); // Calls the check for double declaration
        }
    }
}
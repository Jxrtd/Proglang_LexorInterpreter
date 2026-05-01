package com.lexor.handlers;

import com.lexor.core.SymbolTable;

public class PrintHandler implements CommandHandler {
    @Override
    public void handle(String[] tokens, SymbolTable symbolTable) {
        StringBuilder content = new StringBuilder();
        for (int i = 1; i < tokens.length; i++) {
            content.append(tokens[i]).append(" ");
        }

        String[] parts = content.toString().split("&");

        for (String part : parts) {
            String clean = part.trim();
            if (symbolTable.contains(clean)) {
                System.out.print(symbolTable.get(clean) + " ");
            } else {
                System.out.print(clean.replace("\"", "").replace("'", "") + " ");
            }
        }
        System.out.println();
    }
}
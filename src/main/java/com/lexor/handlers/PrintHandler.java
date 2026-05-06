package com.lexor.handlers;

import com.lexor.core.SymbolTable;

public class PrintHandler implements CommandHandler {
    @Override
    public void handle(String[] tokens, SymbolTable symbolTable) {
        // 1. Reconstruct the full line after "PRINT:" to eliminate spaces
        StringBuilder content = new StringBuilder();
        for (int i = 1; i < tokens.length; i++) {
            content.append(tokens[i]);
        }

        // 2. Split strictly by the '&' character
        String[] parts = content.toString().split("&");

        for (String part : parts) {
            String clean = part.trim();
            
            // --- The Fix for Bracket Formatting ---
            // If the literal is exactly "[[]", we print "["
            if (clean.equals("[[]")) {
                System.out.print("[");
                continue;
            }
            // If the literal is exactly "[]]", we print "]"
            if (clean.equals("[]]")) {
                System.out.print("]");
                continue;
            }

            // 3. Normal variable and literal evaluation
            if (symbolTable.contains(clean)) {
                System.out.print(symbolTable.get(clean));
            } else {
                // Strip standard quotation marks for other literals
                System.out.print(clean.replace("\"", "").replace("'", ""));
            }
        }
        System.out.println(); // One newline at the very end
    }
}
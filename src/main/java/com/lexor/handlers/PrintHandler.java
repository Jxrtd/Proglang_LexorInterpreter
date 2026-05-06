package com.lexor.handlers;

import com.lexor.core.SymbolTable;

public class PrintHandler implements CommandHandler {
    @Override
    public void handle(String[] tokens, SymbolTable symbolTable) {
        // Fallback or legacy support
    }

    @Override
    public void handle(String line, String[] tokens, SymbolTable symbolTable) {
        // Format: PRINT: x & t & z & $ & a_1 & [#] & "last"
        // Extract everything after "PRINT:"
        int colonIdx = line.indexOf(":");
        if (colonIdx == -1) return;
        String content = line.substring(colonIdx + 1).trim();
        
        // Split by '&' but be careful about '&' inside strings
        // For Basic Features 1, we can assume simple split if strings don't contain '&'
        // or implement a more robust split.
        String[] parts = content.split("&");

        for (String part : parts) {
            String clean = part.trim();
            
            if (clean.equals("$")) {
                System.out.println();
                continue;
            }

            // Handle escape codes like [#], [[] or []]
            if (clean.startsWith("[") && clean.endsWith("]")) {
                String escape = clean.substring(1, clean.length() - 1);
                if (escape.equals("[]")) {
                    System.out.print("[");
                } else if (escape.equals("]]")) {
                    System.out.print("]");
                } else {
                    System.out.print(escape);
                }
                continue;
            }

            if (symbolTable.contains(clean)) {
                Object val = symbolTable.get(clean);
                if (val instanceof Boolean) {
                    System.out.print(val.toString().toUpperCase());
                } else {
                    System.out.print(val);
                }
            } else if ((clean.startsWith("\"") && clean.endsWith("\"")) || (clean.startsWith("'") && clean.endsWith("'"))) {
                System.out.print(clean.substring(1, clean.length() - 1));
            } else {
                // Try to evaluate as literal or throw error if it looks like a variable
                if (clean.matches("^-?\\d+$")) {
                    System.out.print(clean);
                } else if (clean.matches("^-?\\d+\\.\\d+$")) {
                    System.out.print(clean);
                } else if (clean.equalsIgnoreCase("TRUE") || clean.equalsIgnoreCase("FALSE")) {
                    System.out.print(clean.toUpperCase());
                } else {
                    throw new RuntimeException("Error: Undefined variable or invalid literal in PRINT: " + clean);
                }
            }
        }
        System.out.println();
    }
}
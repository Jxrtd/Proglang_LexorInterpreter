package com.lexor.core;

import java.util.HashMap;
import java.util.Map;

import com.lexor.handlers.CommandHandler;
import com.lexor.handlers.DeclarationHandler;
import com.lexor.handlers.PrintHandler;
import com.lexor.handlers.ScanHandler;

public class LexorInterpreter {
    private final SymbolTable symbolTable = new SymbolTable();
    private final Map<String, CommandHandler> handlers = new HashMap<>();
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private boolean isInsideScript = false;

    public LexorInterpreter() {
        handlers.put("DECLARE", new DeclarationHandler());
        handlers.put("PRINT:", new PrintHandler()); 
        handlers.put("SCAN", new ScanHandler());
    }

    public void run(String line) {
        // Sanitize smart quotes and trim
        line = line.replace("“", "\"").replace("”", "\"").replace("‘", "'").replace("’", "'");
        String trimmed = line.trim();
        
        if (trimmed.isEmpty() || trimmed.startsWith("%%")) return;
        
        // --- Script Boundary Logic ---
        if (trimmed.equals("START SCRIPT")) {
            if (isInsideScript) {
                System.err.println("Warning: Script already started.");
            }
            isInsideScript = true; 
            return; 
        }
        
        if (trimmed.equals("END SCRIPT")) {
            if (!isInsideScript) {
                throw new RuntimeException("Error: END SCRIPT called without START SCRIPT.");
            }
            isInsideScript = false; 
            return; 
        }

        if (!isInsideScript) return;

        // --- Command Processing ---
        String[] tokens = trimmed.split("\\s+");
        String command = tokens[0].toUpperCase();

        if (handlers.containsKey(command)) {
            handlers.get(command).handle(tokens, symbolTable);
        } else if (trimmed.contains("=")) {
            handleAssignment(trimmed);
        }
    }

    private void handleAssignment(String line) {
        String[] parts = line.split("=");
        String rawExpression = parts[parts.length - 1].trim();
        Object value = evaluator.evaluate(rawExpression, symbolTable);
        
        for (int i = 0; i < parts.length - 1; i++) {
            String varName = parts[i].trim();
            // Optional: Check if variable was declared before assignment
            if (!symbolTable.contains(varName)) {
                throw new RuntimeException("Error: Variable '" + varName + "' must be declared before assignment.");
            }
            symbolTable.set(varName, value);
        }
    }
}
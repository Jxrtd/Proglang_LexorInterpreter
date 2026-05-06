package com.lexor.core;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.lexor.handlers.CommandHandler;
import com.lexor.handlers.DeclarationHandler;
import com.lexor.handlers.PrintHandler;
import com.lexor.handlers.ScanHandler;

public class LexorInterpreter {
    private final SymbolTable symbolTable = new SymbolTable();
    private final Map<String, CommandHandler> handlers = new HashMap<>();
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private boolean foundScriptArea = false;
    private boolean isInsideScript = false;
    private boolean declarationPhase = false;
    
    private static final Pattern VAR_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    public LexorInterpreter() {
        handlers.put("DECLARE", new DeclarationHandler(evaluator));
        handlers.put("PRINT:", new PrintHandler()); 
        handlers.put("SCAN", new ScanHandler());
    }

    public void run(String line) {
        line = line.replace("“", "\"").replace("”", "\"").replace("‘", "'").replace("’", "'");
        String trimmed = line.trim();
        
        if (trimmed.isEmpty() || trimmed.startsWith("%%")) return;
        
        if (trimmed.equals("SCRIPT AREA")) {
            if (foundScriptArea) {
                throw new RuntimeException("Error: SCRIPT AREA already defined.");
            }
            foundScriptArea = true;
            return;
        }

        if (!foundScriptArea) {
            throw new RuntimeException("Error: Program must start with SCRIPT AREA.");
        }

        if (trimmed.equals("START SCRIPT")) {
            if (isInsideScript) {
                throw new RuntimeException("Error: START SCRIPT already called.");
            }
            isInsideScript = true; 
            declarationPhase = true;
            return; 
        }
        
        if (trimmed.equals("END SCRIPT")) {
            if (!isInsideScript) {
                throw new RuntimeException("Error: END SCRIPT called without START SCRIPT.");
            }
            isInsideScript = false; 
            declarationPhase = false;
            return; 
        }

        if (!isInsideScript) {
            throw new RuntimeException("Error: Statement found outside of START SCRIPT and END SCRIPT.");
        }

        String[] tokens = trimmed.split("\\s+");
        String command = tokens[0].toUpperCase();

        if (command.equals("DECLARE")) {
            if (!declarationPhase) {
                throw new RuntimeException("Error: All variable declarations must follow right after the START SCRIPT keyword.");
            }
            handlers.get("DECLARE").handle(trimmed, tokens, symbolTable);
        } else {
            declarationPhase = false; // Once a non-DECLARE statement is found, declaration phase ends
            if (handlers.containsKey(command)) {
                handlers.get(command).handle(trimmed, tokens, symbolTable);
            } else if (trimmed.contains("=")) {
                handleAssignment(trimmed);
            } else {
                throw new RuntimeException("Error: Unknown command or invalid statement: " + trimmed);
            }
        }
    }

    private void handleAssignment(String line) {
        String[] parts = line.split("=");
        String rawExpression = parts[parts.length - 1].trim();
        Object value = evaluator.evaluate(rawExpression, symbolTable);
        
        for (int i = 0; i < parts.length - 1; i++) {
            String varName = parts[i].trim();
            if (!symbolTable.contains(varName)) {
                throw new RuntimeException("Error: Variable '" + varName + "' must be declared before assignment.");
            }
            
            validateType(varName, value);
            symbolTable.set(varName, value);
        }
    }

    private void validateType(String varName, Object value) {
        String expectedType = symbolTable.getType(varName);
        if (expectedType == null) return;

        if (expectedType.equals("INT")) {
            if (!(value instanceof Integer)) {
                throw new RuntimeException("Type Mismatch: Variable '" + varName + "' is INT but assigned " + value.getClass().getSimpleName());
            }
        } else if (expectedType.equals("FLOAT")) {
            if (!(value instanceof Double || value instanceof Integer)) {
                throw new RuntimeException("Type Mismatch: Variable '" + varName + "' is FLOAT but assigned " + value.getClass().getSimpleName());
            }
        } else if (expectedType.equals("BOOL")) {
            if (!(value instanceof Boolean)) {
                throw new RuntimeException("Type Mismatch: Variable '" + varName + "' is BOOL but assigned " + value.getClass().getSimpleName());
            }
        } else if (expectedType.equals("CHAR")) {
            if (!(value instanceof String && ((String) value).length() == 1)) {
                throw new RuntimeException("Type Mismatch: Variable '" + varName + "' is CHAR but assigned " + value.getClass().getSimpleName());
            }
        }
    }

    public static void validateVariableName(String name) {
        if (!VAR_NAME_PATTERN.matcher(name).matches()) {
            throw new RuntimeException("Error: Invalid variable name '" + name + "'. Must start with letter/underscore and contain only letters/digits/underscores.");
        }
    }
}
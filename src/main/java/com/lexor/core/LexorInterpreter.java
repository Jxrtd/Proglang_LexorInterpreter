package com.lexor.core;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import com.lexor.handlers.*;

/**
 * Orchestrates the LEXOR interpreter, managing state and delegating commands.
 */
public class LexorInterpreter {
    private final SymbolTable symbolTable = new SymbolTable();
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private final Map<String, CommandHandler> handlers = new HashMap<>();
    
    private boolean scriptAreaFound = false;
    private boolean insideScript = false;
    private boolean inDeclarationPhase = false;

    public LexorInterpreter() {
        // Register all command handlers with the shared evaluator
        handlers.put("DECLARE", new DeclarationHandler(evaluator));
        handlers.put("PRINT:", new PrintHandler(evaluator)); 
        handlers.put("SCAN:", new ScanHandler());
    }

    public void run(String line) {
        line = normalizeLine(line);
        if (shouldSkip(line)) return;

        // 1. Structural Validation
        if (handleStructure(line)) return;

        validateState();

        // 2. Command Execution
        String[] tokens = line.split("\\s+");
        String command = tokens[0].toUpperCase();

        if (command.equals("DECLARE")) {
            executeDeclaration(line, tokens);
        } else {
            inDeclarationPhase = false; // End declaration phase on first non-DECLARE command
            executeCommand(line, tokens, command);
        }
    }

    private String normalizeLine(String line) {
        return line.replace("“", "\"").replace("”", "\"").replace("‘", "'").replace("’", "'").trim();
    }

    private boolean shouldSkip(String line) {
        return line.isEmpty() || line.startsWith("%%");
    }

    private boolean handleStructure(String line) {
        if (line.equals("SCRIPT AREA")) {
            if (scriptAreaFound) throw new RuntimeException("Error: SCRIPT AREA redefined.");
            scriptAreaFound = true;
            return true;
        }
        if (line.equals("START SCRIPT")) {
            if (!scriptAreaFound) throw new RuntimeException("Error: Missing SCRIPT AREA.");
            if (insideScript) throw new RuntimeException("Error: START SCRIPT repeated.");
            insideScript = true;
            inDeclarationPhase = true;
            return true;
        }
        if (line.equals("END SCRIPT")) {
            if (!insideScript) throw new RuntimeException("Error: END SCRIPT without START SCRIPT.");
            insideScript = false;
            inDeclarationPhase = false;
            return true;
        }
        return false;
    }

    private void validateState() {
        if (!scriptAreaFound) throw new RuntimeException("Error: Program must start with SCRIPT AREA.");
        if (!insideScript) throw new RuntimeException("Error: Statement outside START/END SCRIPT.");
    }

    private void executeDeclaration(String line, String[] tokens) {
        if (!inDeclarationPhase) {
            throw new RuntimeException("Error: Declarations must follow START SCRIPT immediately.");
        }
        handlers.get("DECLARE").handle(line, tokens, symbolTable);
    }

    private void executeCommand(String line, String[] tokens, String command) {
        if (handlers.containsKey(command)) {
            handlers.get(command).handle(line, tokens, symbolTable);
        } else if (line.contains("=")) {
            handleAssignment(line);
        } else {
            throw new RuntimeException("Error: Unknown command: " + command);
        }
    }

    private void handleAssignment(String line) {
        String[] parts = line.split("=");
        Object value = evaluator.evaluate(parts[parts.length - 1].trim(), symbolTable);
        
        for (int i = 0; i < parts.length - 1; i++) {
            String varName = parts[i].trim();
            validateVariableForAssignment(varName, value);
            symbolTable.set(varName, value);
        }
    }

    private void validateVariableForAssignment(String varName, Object value) {
        if (!symbolTable.contains(varName)) {
            throw new RuntimeException("Error: Variable '" + varName + "' not declared.");
        }
        String type = symbolTable.getType(varName);
        if (!TypeSystem.isCompatible(type, value)) {
            throw new RuntimeException("Type Mismatch: " + varName + " is " + type + " but got " + value.getClass().getSimpleName());
        }
    }

    public static void validateVariableName(String name) {
        if (!Pattern.matches("^[a-zA-Z_][a-zA-Z0-9_]*$", name)) {
            throw new RuntimeException("Error: Invalid variable name: " + name);
        }
    }
}
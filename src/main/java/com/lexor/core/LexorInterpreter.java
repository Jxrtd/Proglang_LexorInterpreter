package com.lexor.core;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import com.lexor.handlers.*;

// Orchestrates the LEXOR interpreter, managing state and delegating commands.
public class LexorInterpreter {
    private final SymbolTable symbolTable = new SymbolTable();
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private final Map<String, CommandHandler> handlers = new HashMap<>();
    
    private boolean scriptAreaFound = false;
    private boolean insideScript = false;
    private boolean inDeclarationPhase = false;

    // Registers all command handlers with a shared evaluator.
    public LexorInterpreter() {
        handlers.put("DECLARE", new DeclarationHandler(evaluator));
        handlers.put("PRINT:", new PrintHandler(evaluator)); 
        handlers.put("SCAN:", new ScanHandler());
    }

    // Executes a single line of LEXOR source code.
    public void run(String line) {
        line = normalizeLine(line);
        if (shouldSkip(line)) return;
        if (handleStructure(line)) return;
        validateExecutionState();

        String[] tokens = line.split("\\s+");
        String command = tokens[0].toUpperCase();

        if (command.equals("DECLARE")) {
            executeDeclaration(line, tokens);
        } else {
            inDeclarationPhase = false;
            executeCommand(line, tokens, command);
        }
    }

    // Trims whitespace, normalizes quotes, and strips LEXOR comments.
    private String normalizeLine(String line) {
        int commentIdx = line.indexOf("%%");
        if (commentIdx != -1) line = line.substring(0, commentIdx);
        return line.replace("“", "\"").replace("”", "\"").replace("‘", "'").replace("’", "'").trim();
    }

    // Checks if the line is empty after stripping comments.
    private boolean shouldSkip(String line) {
        return line.isEmpty();
    }

    // Processes SCRIPT AREA, START SCRIPT, and END SCRIPT directives.
    private boolean handleStructure(String line) {
        if (line.equals("SCRIPT AREA")) {
            if (scriptAreaFound) throw new LexorException("Error: SCRIPT AREA redefined.");
            scriptAreaFound = true;
            return true;
        }
        if (line.equals("START SCRIPT")) {
            if (!scriptAreaFound) throw new LexorException("Error: Missing SCRIPT AREA.");
            if (insideScript) throw new LexorException("Error: START SCRIPT already active.");
            insideScript = true;
            inDeclarationPhase = true;
            return true;
        }
        if (line.equals("END SCRIPT")) {
            if (!insideScript) throw new LexorException("Error: END SCRIPT called without START SCRIPT.");
            insideScript = false;
            inDeclarationPhase = false;
            return true;
        }
        return false;
    }

    // Ensures the interpreter is in a valid state to execute commands.
    private void validateExecutionState() {
        if (!scriptAreaFound) throw new LexorException("Error: Program must start with SCRIPT AREA.");
        if (!insideScript) throw new LexorException("Error: Statement found outside of script boundaries.");
    }

    // Handles the execution of DECLARE statements.
    private void executeDeclaration(String line, String[] tokens) {
        if (!inDeclarationPhase) throw new LexorException("Error: All variable declarations must follow right after START SCRIPT.");
        handlers.get("DECLARE").handle(line, tokens, symbolTable);
    }

    // Delegating logic for recognized commands or assignments.
    private void executeCommand(String line, String[] tokens, String command) {
        if (handlers.containsKey(command)) {
            handlers.get(command).handle(line, tokens, symbolTable);
        } else if (line.contains("=")) {
            handleAssignment(line);
        } else {
            throw new LexorException("Error: Unknown command or invalid statement: " + command);
        }
    }

    // Manages variable assignments, including chained assignments.
    private void handleAssignment(String line) {
        String[] parts = line.split("(?<![<>=])=(?![=])");
        Object value = evaluator.evaluate(parts[parts.length - 1].trim(), symbolTable);
        for (int i = 0; i < parts.length - 1; i++) {
            String name = parts[i].trim();
            validateVariable(name, value);
            symbolTable.set(name, value);
        }
    }

    // Validates that a variable is declared and compatible with the value.
    private void validateVariable(String name, Object value) {
        if (!symbolTable.contains(name)) throw new LexorException("Error: Variable '" + name + "' must be declared before assignment.");
        String type = symbolTable.getType(name);
        if (!TypeSystem.isCompatible(type, value)) throw new LexorException("Type Mismatch: Variable '" + name + "' is " + type + " but assigned " + value.getClass().getSimpleName());
    }

    // Validates variable names according to LEXOR naming conventions.
    public static void validateVariableName(String name) {
        if (!Pattern.matches("^[a-zA-Z_][a-zA-Z0-9_]*$", name)) throw new LexorException("Error: Invalid variable name '" + name + "'.");
    }
}
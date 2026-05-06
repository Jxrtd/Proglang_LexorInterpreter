package com.lexor.handlers;

import java.util.Scanner;

import com.lexor.core.SymbolTable;

/**
 * Handles the SCAN: command for reading user input into variables.
 * Supports comma-separated variable lists and enforces type safety.
 */
public class ScanHandler implements CommandHandler {
    private final Scanner inputScanner = new Scanner(System.in);

    @Override
    public void handle(String[] tokens, SymbolTable symbolTable) {
        // Handled by the full-line version for better space/comma handling
    }

    /**
     * Processes the SCAN: command using the full line for accurate parsing.
     * @param line The original statement line.
     * @param tokens Pre-split tokens.
     * @param symbolTable The project's symbol table.
     */
    @Override
    public void handle(String line, String[] tokens, SymbolTable symbolTable) {
        // Expected format: SCAN: var1, var2, ...
        int colonIdx = line.indexOf(":");
        if (colonIdx == -1) {
            throw new RuntimeException("Error: Invalid SCAN syntax. Missing colon after SCAN.");
        }

        String content = line.substring(colonIdx + 1).trim();
        if (content.isEmpty()) {
            throw new RuntimeException("Error: SCAN: command requires at least one variable name.");
        }

        // Split by commas to support multiple variables
        String[] varNames = content.split(",");

        for (String varName : varNames) {
            varName = varName.trim();
            if (varName.isEmpty()) continue;

            // 1. Check if variable is declared
            if (!symbolTable.contains(varName)) {
                throw new RuntimeException("Error: Variable '" + varName + "' must be declared before SCAN.");
            }

            String type = symbolTable.getType(varName);
            System.out.print("Input value for " + varName + " (" + type + "): ");
            
            if (!inputScanner.hasNextLine()) {
                throw new RuntimeException("Error: No input provided for variable '" + varName + "'.");
            }

            String input = inputScanner.nextLine().trim();
            Object value = parseInput(input, type, varName);
            
            // 2. Store the validated value
            symbolTable.set(varName, value);
        }
    }

    /**
     * Parses the string input based on the expected LEXOR data type.
     * @param input The raw user input.
     * @param type The target LEXOR type (INT, CHAR, BOOL, FLOAT).
     * @param varName The name of the variable (for error reporting).
     * @return The parsed object.
     */
    private Object parseInput(String input, String type, String varName) {
        try {
            switch (type) {
                case "INT":
                    return Integer.parseInt(input);
                case "FLOAT":
                    return Double.parseDouble(input);
                case "BOOL":
                    if (input.equalsIgnoreCase("TRUE")) return true;
                    if (input.equalsIgnoreCase("FALSE")) return false;
                    throw new RuntimeException("Type Mismatch: '" + input + "' is not a valid BOOL for '" + varName + "'.");
                case "CHAR":
                    if (input.length() != 1) {
                        throw new RuntimeException("Type Mismatch: CHAR variable '" + varName + "' requires a single symbol.");
                    }
                    return input;
                default:
                    return input;
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Type Mismatch: Cannot parse '" + input + "' as " + type + " for variable '" + varName + "'.");
        }
    }
}
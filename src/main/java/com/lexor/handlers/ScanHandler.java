package com.lexor.handlers;

import java.util.Scanner;

import com.lexor.core.LexorException;
import com.lexor.core.SymbolTable;

// Handles the SCAN: command for reading user input.
public class ScanHandler implements CommandHandler {
    private final Scanner inputScanner = new Scanner(System.in);

    // Legacy handler placeholder.
    @Override public void handle(String[] tokens, SymbolTable symbolTable) {}

    // Processes inputs for multiple variables separated by commas.
    @Override public void handle(String line, String[] tokens, SymbolTable symbolTable) {
        int idx = line.indexOf(":");
        if (idx == -1) throw new LexorException("Error: SCAN missing colon.");
        for (String varName : line.substring(idx + 1).split(",")) {
            String name = varName.trim();
            if (name.isEmpty()) continue;
            if (!symbolTable.contains(name)) throw new LexorException("Error: Variable '" + name + "' not declared.");
            String type = symbolTable.getType(name);
            symbolTable.set(name, parseInput(name, inputScanner.nextLine().trim(), type));
        }
    }

    // Validates and converts user input based on the expected LEXOR type.
    private Object parseInput(String name, String input, String type) {
        try {
            return switch (type) {
                case "INT" -> Integer.valueOf(input);
                case "FLOAT" -> Double.valueOf(input);
                case "BOOL" -> input.equalsIgnoreCase("TRUE") ? true : input.equalsIgnoreCase("FALSE") ? false : throwError(name, input, type);
                case "CHAR" -> input.length() == 1 ? input : throwError(name, input, type);
                default -> input;
            };
        } catch (Exception e) { throw new LexorException("Type Mismatch: '" + input + "' is not a valid " + type); }
    }

    // Helper for reporting type mismatch errors.
    private Object throwError(String name, String input, String type) { throw new LexorException("Type Mismatch: '" + input + "' is not " + type); }
}
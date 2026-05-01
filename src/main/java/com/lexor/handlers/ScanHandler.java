package com.lexor.handlers;

import java.util.Scanner;

import com.lexor.core.SymbolTable;

public class ScanHandler implements CommandHandler {
    private final Scanner inputScanner = new Scanner(System.in);

    @Override
    public void handle(String[] tokens, SymbolTable symbolTable) {
        // Format: SCAN x
        String varName = tokens[1];
        System.out.print("Input for " + varName + ": ");
        String value = inputScanner.nextLine();
        
        if (value.equalsIgnoreCase("TRUE") || value.equalsIgnoreCase("FALSE")) {
            symbolTable.set(varName, Boolean.valueOf(value)); 
        } else if (value.matches("-?\\d+")) {
            symbolTable.set(varName, Integer.valueOf(value));
        } else {
            symbolTable.set(varName, value);
        }
    }
}
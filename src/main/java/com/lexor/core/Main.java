package com.lexor.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Entry point for the LEXOR interpreter.
 * Reads the 'program.lexor' file and executes it line by line.
 */
public class Main {
    public static void main(String[] args) {
        String filename = "program.lexor";
        File file = new File(filename);
        LexorInterpreter interpreter = new LexorInterpreter();

        try (Scanner fileScanner = new Scanner(file)) {
            System.out.println("--- Executing: " + filename + " ---");
            
            int lineNumber = 0;
            while (fileScanner.hasNextLine()) {
                lineNumber++;
                String line = fileScanner.nextLine();
                try {
                    interpreter.run(line);
                } catch (RuntimeException e) {
                    // Catch and report runtime errors with line number information
                    System.err.println("Runtime Error at line " + lineNumber + ": " + e.getMessage());
                    break; // Stop execution on first error
                }
            }
            
            System.out.println("--- Execution Complete ---");
            
        } catch (FileNotFoundException e) {
            System.err.println("Error: Could not find '" + filename + "' in " + file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
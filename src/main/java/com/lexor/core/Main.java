package com.lexor.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

// Entry point for the LEXOR interpreter.
public class Main {
    // Reads and executes the program.lexor file.
    public static void main(String[] args) {
        String filename = "program.lexor";
        File file = new File(filename);
        LexorInterpreter interpreter = new LexorInterpreter();
        try (Scanner fileScanner = new Scanner(file)) {
            System.out.println("--- Executing: " + filename + " ---");
            int lineNum = 0;
            while (fileScanner.hasNextLine()) {
                lineNum++;
                try {
                    interpreter.run(fileScanner.nextLine());
                } catch (LexorException e) {
                    System.err.println("Runtime Error at line " + lineNum + ": " + e.getMessage());
                    break;
                }
            }
            System.out.println("--- Execution Complete ---");
        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found: " + filename);
        } catch (Exception e) {
            System.err.println("Fatal Error: " + e.getMessage());
        }
    }
}
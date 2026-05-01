package com.lexor.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        File file = new File("interpreter/program.lexor");
        LexorInterpreter interpreter = new LexorInterpreter();

        try (Scanner scanner = new Scanner(file)) {
            System.out.println("--- Executing program.lexor ---");
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                interpreter.run(line);
            }
            
            System.out.println("--- Execution Complete ---");
            
        } catch (FileNotFoundException e) {
            System.err.println("Error: Could not find 'program.lexor' in " + file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Runtime Error: " + e.getMessage());
        }
    }
}
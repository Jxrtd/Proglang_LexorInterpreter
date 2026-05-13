package com.lexor.core;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.lexor.ast.Stmt;
import com.lexor.interpreter.Interpreter;
import com.lexor.lexer.Lexer;
import com.lexor.lexer.Token;
import com.lexor.parser.Parser;

// The main entry point that wires the Lexer, Parser, and Interpreter together.
public class Main {
    // Reads source file and runs the full compiler pipeline.
    public static void main(String[] args) {
        String filename = "program.lexor";
        try {
            String source = Files.readString(Paths.get(filename));
            System.out.println("--- Executing: " + filename + " ---");

            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.scanTokens();

            Parser parser = new Parser(tokens);
            List<Stmt> statements = parser.parse();

            Interpreter interpreter = new Interpreter();
            interpreter.interpret(statements);

            System.out.println("--- Execution Complete ---");

        } catch (LexorException e) { System.err.println(e.getMessage());
            
        } catch (Exception e) { System.err.println("Fatal Error: " + e.getMessage()); }
    }
}
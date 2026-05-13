package com.lexor.core;

// Custom exception that includes line number information for error reporting.
public class LexorException extends RuntimeException {
    // Constructs the exception with the specific line number and message.
    public LexorException(int line, String message) {
        super("Runtime Error at line " + line + ": " + message);
    }
}
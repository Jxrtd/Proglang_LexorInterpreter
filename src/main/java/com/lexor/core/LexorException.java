package com.lexor.core;

// Custom runtime exception for LEXOR interpreter.
public class LexorException extends RuntimeException {
    public LexorException(String message) {
        super(message);
    }
}
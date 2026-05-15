package com.lexor.core;

public class LexorException extends RuntimeException {
    public LexorException(int line, String message) {
        super("Runtime Error at line " + line + ": " + message);
    }
}
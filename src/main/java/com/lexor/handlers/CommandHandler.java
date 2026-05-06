package com.lexor.handlers;

import com.lexor.core.SymbolTable;

public interface CommandHandler {
    void handle(String[] tokens, SymbolTable symbolTable);
    default void handle(String line, String[] tokens, SymbolTable symbolTable) {
        handle(tokens, symbolTable);
    }
}
package com.lexor.handlers;

import com.lexor.core.SymbolTable;

// Common interface for handling LEXOR commands.
public interface CommandHandler {
    // Processes tokens split by whitespace.
    void handle(String[] tokens, SymbolTable symbolTable);
    // Processes the full original line to preserve formatting.
    default void handle(String line, String[] tokens, SymbolTable symbolTable) {
        handle(tokens, symbolTable);
    }
}
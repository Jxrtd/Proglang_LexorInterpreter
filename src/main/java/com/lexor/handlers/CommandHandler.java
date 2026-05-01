package com.lexor.handlers;

import com.lexor.core.SymbolTable;

public interface CommandHandler {
    void handle(String[] tokens, SymbolTable symbolTable);
}
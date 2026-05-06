package com.lexor.handlers;

import com.lexor.core.ExpressionEvaluator;
import com.lexor.core.SymbolTable;

/**
 * Handles the PRINT: command. Evaluates expressions separated by '&'.
 */
public class PrintHandler implements CommandHandler {
    private final ExpressionEvaluator evaluator;

    public PrintHandler(ExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public void handle(String[] tokens, SymbolTable symbolTable) {
        // Not used
    }

    @Override
    public void handle(String line, String[] tokens, SymbolTable symbolTable) {
        String content = extractContent(line);
        String[] parts = content.split("&");

        for (String part : parts) {
            String clean = part.trim();
            if (clean.equals("$")) {
                System.out.println();
            } else if (isEscapeCode(clean)) {
                printEscapeCode(clean);
            } else {
                Object value = evaluator.evaluate(clean, symbolTable);
                printValue(value);
            }
        }
        System.out.println();
    }

    private String extractContent(String line) {
        int colonIdx = line.indexOf(":");
        if (colonIdx == -1) throw new RuntimeException("Error: Missing ':' in PRINT statement.");
        return line.substring(colonIdx + 1).trim();
    }

    private boolean isEscapeCode(String part) {
        return part.startsWith("[") && part.endsWith("]");
    }

    private void printEscapeCode(String code) {
        String inner = code.substring(1, code.length() - 1);
        if (inner.equals("[]")) System.out.print("[");
        else if (inner.equals("]]")) System.out.print("]");
        else System.out.print(inner);
    }

    private void printValue(Object value) {
        if (value instanceof Boolean) {
            System.out.print(value.toString().toUpperCase());
        } else {
            System.out.print(value);
        }
    }
}
package com.lexor.handlers;

import com.lexor.core.ExpressionEvaluator;
import com.lexor.core.LexorException;
import com.lexor.core.SymbolTable;

// Handles the PRINT: command for formatted output.
public class PrintHandler implements CommandHandler {
    private final ExpressionEvaluator evaluator;
    public PrintHandler(ExpressionEvaluator evaluator) { this.evaluator = evaluator; }

    // Legacy handler placeholder.
    @Override public void handle(String[] tokens, SymbolTable symbolTable) {}

    // Processes concatenated expressions separated by '&'.
    @Override public void handle(String line, String[] tokens, SymbolTable symbolTable) {
        int idx = line.indexOf(":");
        if (idx == -1) throw new LexorException("Error: PRINT missing colon.");
        for (String part : line.substring(idx + 1).split("&")) {
            String clean = part.trim();
            if (clean.equals("$")) System.out.println();
            else if (clean.startsWith("[") && clean.endsWith("]")) printEscape(clean);
            else printValue(evaluator.evaluate(clean, symbolTable));
        }
        System.out.println();
    }

    // Resolves and displays escape codes like [#] or [[] or []].
    private void printEscape(String code) {
        String inner = code.substring(1, code.length() - 1);
        if (inner.equals("[]")) System.out.print("[");
        else if (inner.equals("]]")) System.out.print("]");
        else System.out.print(inner);
    }

    // Prints a generic object value with uppercase for booleans.
    private void printValue(Object val) {
        if (val instanceof Boolean) System.out.print(val.toString().toUpperCase());
        else System.out.print(val);
    }
}
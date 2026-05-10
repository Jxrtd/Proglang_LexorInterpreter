package com.lexor.core;

// Utility class for checking LEXOR data type compatibility.
public class TypeSystem {
    // Checks if the evaluated value matches the declared LEXOR type.
    public static boolean isCompatible(String type, Object value) {
        if (value == null) return true;
        return switch (type) {
            case "INT" -> value instanceof Integer;
            case "FLOAT" -> value instanceof Double || value instanceof Integer;
            case "BOOL" -> value instanceof Boolean;
            case "CHAR" -> value instanceof String && ((String) value).length() == 1;
            default -> false;
        };
    }
}
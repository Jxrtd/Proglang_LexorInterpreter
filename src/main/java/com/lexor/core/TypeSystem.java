package com.lexor.core;

/**
 * Static utility for LEXOR type checking.
 */
public class TypeSystem {
    public static boolean isCompatible(String type, Object value) {
        if (value == null) return true;
        switch (type) {
            case "INT": return value instanceof Integer;
            case "FLOAT": return value instanceof Double || value instanceof Integer;
            case "BOOL": return value instanceof Boolean;
            case "CHAR": return value instanceof String && ((String) value).length() == 1;
            default: return false;
        }
    }
}
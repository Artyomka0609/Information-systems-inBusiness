package ru.edu.pr03;

import java.util.Locale;

/** Разбор CSV */
public final class RequestCsvParser {
    private static final String HEADER = "amount;urgent;risk;personal";

    public void validateHeader(String header) {
        
        if (header != null && header.startsWith("\uFEFF")) {
            header = header.substring(1);
        }
        if (header == null || !HEADER.equals(header.trim())) {
            throw new IllegalArgumentException("expected header: " + HEADER);
        }
    }

    public ApprovalRequest parse(String line) {
        if (line == null) {
            throw new IllegalArgumentException("CSV line is required");
        }
        String[] fields = line.split(";", -1);
        if (fields.length != 4) {
            throw new IllegalArgumentException("expected 4 fields");
        }
        return new ApprovalRequest(parseAmount(fields[0]),
                parseBoolean(fields[1], "urgent"), parseRisk(fields[2]),
                parseBoolean(fields[3], "personal"));
    }

    public static long parseAmount(String text) {
        String value = required(text, "amount");
        if (!value.matches("[0-9]+")) {
            throw new IllegalArgumentException("amount must be a non-negative integer");
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("amount exceeds Long.MAX_VALUE", e);
        }
    }

    public static boolean parseBoolean(String text, String field) {
        String value = required(text, field);
        if ("true".equalsIgnoreCase(value)) {
            return true;
        } else if ("false".equalsIgnoreCase(value)) {
            return false;
        } else {
            throw new IllegalArgumentException(field + " must be true or false");
        }
    }

    public static RiskLevel parseRisk(String text) {
        String value = required(text, "risk");
        try {
            return RiskLevel.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("risk must be LOW, MEDIUM or HIGH", e);
        }
    }

    private static String required(String text, String field) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return text.trim();
    }
}

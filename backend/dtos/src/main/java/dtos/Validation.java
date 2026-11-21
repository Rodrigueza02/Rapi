package dtos;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

final class Validation {

    private static final Pattern PHONE = Pattern.compile("\\+?[0-9]{7,15}");

    private Validation() {
    }

    static void requireNonNull(Object value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " must not be null");
        }
    }

    static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
    }

    static void requirePhone(String value, String field) {
        requireText(value, field);
        if (!PHONE.matcher(value).matches()) {
            throw new IllegalArgumentException(field + " must have 7 to 15 digits (optional leading +)");
        }
    }

    static void requirePositive(BigDecimal value, String field) {
        requireNonNull(value, field);
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be greater than zero");
        }
    }

    /** Valida la lista y devuelve una copia inmutable (copia defensiva). */
    static List<String> requireNonEmptyTexts(List<String> values, String field) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be null or empty");
        }
        values.forEach(item -> requireText(item, field + " item"));
        return List.copyOf(values);
    }
}
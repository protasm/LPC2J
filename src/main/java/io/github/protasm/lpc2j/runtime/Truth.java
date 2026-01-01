package io.github.protasm.lpc2j.runtime;

/**
 * Utility helpers for LPC truthiness semantics.
 */
public final class Truth {
    private Truth() {
        // static helper class
    }

    /**
     * Convert any LPC value to a boolean according to simple truthiness rules:
     * <ul>
     *   <li>{@code null} is false.</li>
     *   <li>{@link Boolean} values are returned as-is.</li>
     *   <li>{@link Number} values are true when non-zero.</li>
     *   <li>All other non-null values are treated as true.</li>
     * </ul>
     *
     * @param value a value produced by compiled LPC code
     * @return {@code true} when the value should be considered truthy
     */
    public static boolean isTruthy(Object value) {
        if (value == null) {
            return false;
        }

        if (value instanceof Boolean b) {
            return b;
        }

        if (value instanceof Number number) {
            return number.doubleValue() != 0.0d;
        }

        return true;
    }
}

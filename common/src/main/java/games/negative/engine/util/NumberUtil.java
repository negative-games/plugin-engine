package games.negative.engine.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * Represents a number utility class used to handle numbers, parse them, etc.
 */
public final class NumberUtil {

    /*
     * Format a number to a fancy format.
     */
    private static final ThreadLocal<DecimalFormat> FANCY_FORMAT = ThreadLocal.withInitial(
            () -> new DecimalFormat("###,###,###,###,###.##")
    );

    private static final String SUFFIXES = "kMBTQqSsOND";
    private static final double LOG_THOUSAND = Math.log(1000);

    private NumberUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Parse a number to a fancy format.
     *
     * @param number Number to parse
     * @return Parsed number
     */
    public static String decimalFormat(int number) {
        return FANCY_FORMAT.get().format(number);
    }

    /**
     * Parse a number to a fancy format.
     *
     * @param number Number to parse
     * @return Parsed number
     */
    public static String decimalFormat(long number) {
        return FANCY_FORMAT.get().format(number);
    }

    /**
     * Parse a number to a fancy format.
     *
     * @param number Number to parse
     * @return Parsed number
     */
    public static String decimalFormat(double number) {
        return FANCY_FORMAT.get().format(number);
    }

    /**
     * Parse a number to a fancy format.
     *
     * @param number Number to parse
     * @return Parsed number
     */
    public static String decimalFormat(float number) {
        return FANCY_FORMAT.get().format(number);
    }

    /**
     * Parse a number to a fancy format.
     *
     * @param number Number to parse
     * @return Parsed number
     */
    public static String decimalFormat(short number) {
        return FANCY_FORMAT.get().format(number);
    }

    /**
     * Parse a number to a fancy format.
     *
     * @param number Number to parse
     * @return Parsed number
     */
    public static String decimalFormat(byte number) {
        return FANCY_FORMAT.get().format(number);
    }

    /**
     * Parse a number to a fancy format.
     * @param number Number to parse
     * @return Parsed number
     */
    public static String decimalFormat(BigDecimal number) {
        return FANCY_FORMAT.get().format(number);
    }

    /**
     * Parse a number to a fancy format.
     * @param number Number to parse
     * @return Parsed number
     */
    public static String decimalFormat(BigInteger number) {
        return FANCY_FORMAT.get().format(number);
    }

    /**
     * Parses an integer from a string.
     * @param input String input
     * @return Parsed integer
     */
    public static Optional<Integer> parseInteger(String input) {
        return parse(input, Integer::valueOf);
    }

    /**
     * Parses a long from a string.
     * @param input String input
     * @return Parsed long
     */
    public static Optional<Long> parseLong(String input) {
        return parse(input, Long::valueOf);
    }

    /**
     * Parses a double from a string.
     * @param input String input
     * @return Parsed double
     */
    public static Optional<Double> parseDouble(String input) {
        return parse(input, Double::valueOf);
    }

    /**
     * Parses a float from a string.
     * @param input String input
     * @return Parsed float
     */
    public static Optional<Float> parseFloat(String input) {
        return parse(input, Float::valueOf);
    }

    /**
     * Parses a short from a string.
     * @param input String input
     * @return Parsed short
     */
    public static Optional<Short> parseShort(String input) {
        return parse(input, Short::valueOf);
    }

    /**
     * Parses a byte from a string.
     * @param input String input
     * @return Parsed byte
     */
    public static Optional<Byte> parseByte(String input) {
        return parse(input, Byte::valueOf);
    }

    /**
     * This method will convert a number to a fancy version of
     * the provided number such as 1st, 2nd, 3rd, 4th, etc.
     *
     * @param number Number to convert
     * @return Fancy version of the number
     */
    public static String fancy(int number) {
        return formatOrdinal(number, decimalFormat(number));
    }

    /**
     * This method will convert a number to a fancy version of
     * the provided number such as 1st, 2nd, 3rd, 4th, etc.
     *
     * @param number Number to convert
     * @return Fancy version of the number
     */
    public static String fancy(long number) {
        return formatOrdinal(number, decimalFormat(number));
    }

    /**
     * This method will convert a number to a fancy version of
     * the provided number such as 1st, 2nd, 3rd, 4th, etc.
     *
     * @param number Number to convert
     * @return Fancy version of the number
     */
    public static String fancy(double number) {
        return formatOrdinal(number, decimalFormat(number));
    }

    /**
     * This method will convert a number to a fancy version of
     * the provided number such as 1st, 2nd, 3rd, 4th, etc.
     *
     * @param number Number to convert
     * @return Fancy version of the number
     */
    public static String fancy(float number) {
        return formatOrdinal(number, decimalFormat(number));
    }

    /**
     * This method will convert a number to a fancy version of
     * the provided number such as 1st, 2nd, 3rd, 4th, etc.
     *
     * @param number Number to convert
     * @return Fancy version of the number
     */
    public static String fancy(short number) {
        return formatOrdinal(number, decimalFormat(number));
    }

    /**
     * This method will convert a number to a fancy version of
     * the provided number such as 1st, 2nd, 3rd, 4th, etc.
     *
     * @param number Number to convert
     * @return Fancy version of the number
     */
    public static String fancy(byte number) {
        return formatOrdinal(number, decimalFormat(number));
    }

    /**
     * Condenses a number into a shorter version using suffixes.
     * @param number Number to condense
     * @return Condensed number
     */
    public static String condense(int number) {
        return condense(number, null);
    }

    /**
     * Condenses a number into a shorter version using suffixes.
     * @param number Number to condense
     * @param set Set of suffixes to use
     * @return Condensed number
     */
    public static String condense(int number, final char[] set) {
        return condense((double) number, set);
    }

    /**
     * Condenses a number into a shorter version using suffixes.
     * @param number Number to condense
     * @return Condensed number
     */
    public static String condense(double number) {
        return condense(number, null);
    }

    /**
     * Condenses a number into a shorter version using suffixes.
     * @param number Number to condense
     * @param set Set of suffixes to use
     * @return Condensed number
     */
    public static String condense(double number, final char[] set) {
        double absolute = Math.abs(number);
        if (absolute < 1000) return String.valueOf(number);

        int exp = (int) (Math.log(absolute) / LOG_THOUSAND);
        String suffixes = suffixes(set);
        char suffix = suffixes.charAt(Math.min(exp - 1, suffixes.length() - 1));

        return String.format(Locale.ROOT, "%.1f%c", number / Math.pow(1000, exp), suffix);
    }

    /**
     * Condenses a number into a shorter version using suffixes.
     * @param number Number to condense
     * @return Condensed number
     */
    public static String condense(long number) {
        return condense(number, null);
    }

    /**
     * Condenses a number into a shorter version using suffixes.
     * @param number Number to condense
     * @param set Set of suffixes to use
     * @return Condensed number
     */
    public static String condense(long number, final char[] set) {
        return condense((double) number, set);
    }

    /**
     * Condenses a number into a shorter version using suffixes.
     * @param number Number to condense
     * @return Condensed number
     */
    public static String condense(BigDecimal number) {
        return condense(number, null);
    }

    /**
     * Condenses a number into a shorter version using suffixes.
     * @param number Number to condense
     * @param set Set of suffixes to use
     * @return Condensed number
     */
    public static String condense(BigDecimal number, final char[] set) {
        BigDecimal thousand = BigDecimal.valueOf(1000);
        BigDecimal absolute = number.abs();
        if (absolute.compareTo(thousand) < 0) {
            return number.stripTrailingZeros().toPlainString();
        }

        int integerDigits = absolute.precision() - absolute.scale();
        int exp = Math.max(1, (integerDigits - 1) / 3);
        String suffixes = suffixes(set);
        char suffix = suffixes.charAt(Math.min(exp - 1, suffixes.length() - 1));

        BigDecimal result = number.divide(thousand.pow(exp), 1, RoundingMode.HALF_UP);
        return String.format(Locale.ROOT, "%.1f%c", result, suffix);
    }

    /**
     * Condenses a number into a shorter version using suffixes.
     * @param number Number to condense
     * @return Condensed number
     */
    public static String condense(BigInteger number) {
        return condense(number, null);
    }

    /**
     * Condenses a number into a shorter version using suffixes.
     * @param number Number to condense
     * @param set Set of suffixes to use
     * @return Condensed number
     */
    public static String condense(BigInteger number, final char[] set) {
        return condense(new BigDecimal(number), set);
    }

    private static String formatOrdinal(double number, String formattedNumber) {
        if (number % 100 >= 11 && number % 100 <= 13) {
            return formattedNumber + "th";
        }

        return switch ((int) (number % 10)) {
            case 1 -> formattedNumber + "st";
            case 2 -> formattedNumber + "nd";
            case 3 -> formattedNumber + "rd";
            default -> formattedNumber + "th";
        };
    }

    private static String formatOrdinal(long number, String formattedNumber) {
        if (number % 100 >= 11 && number % 100 <= 13) {
            return formattedNumber + "th";
        }

        return switch ((int) (number % 10)) {
            case 1 -> formattedNumber + "st";
            case 2 -> formattedNumber + "nd";
            case 3 -> formattedNumber + "rd";
            default -> formattedNumber + "th";
        };
    }

    private static String suffixes(char[] set) {
        return set == null ? SUFFIXES : String.valueOf(set);
    }

    private static <T> Optional<T> parse(String input, NumberParser<T> parser) {
        try {
            return Optional.of(parser.parse(input.trim()));
        } catch (NullPointerException | NumberFormatException exception) {
            return Optional.empty();
        }
    }

    @FunctionalInterface
    private interface NumberParser<T> {

        T parse(String input);
    }
}

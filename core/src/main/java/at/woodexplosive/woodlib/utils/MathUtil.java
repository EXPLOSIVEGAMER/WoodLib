package at.woodexplosive.woodlib.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Small math helpers used throughout WoodLib.
 */
public final class MathUtil {
    private MathUtil() {}

    /**
     * Returns a list of integers from {@code first} (inclusive) to {@code last} (exclusive).
     * @param first the first value (inclusive)
     * @param last the last value (exclusive)
     * @return the range as a list
     */
    @Contract(pure = true)
    public static @NotNull List<Integer> intRange(int first, int last) {
        return IntStream.range(first, last).boxed().toList();
    }

    /**
     * Returns a list of integers from {@code first} to {@code last}, both inclusive.
     * @param first the first value (inclusive)
     * @param last the last value (inclusive)
     * @return the range as a list
     */
    @Contract(pure = true)
    public static @NotNull List<Integer> intRangeClosed(int first, int last) {
        return IntStream.rangeClosed(first, last).boxed().toList();
    }
}

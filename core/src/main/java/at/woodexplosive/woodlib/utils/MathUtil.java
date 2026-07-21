package at.woodexplosive.woodlib.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public final class MathUtil {
    private MathUtil() {};

    public static List<Integer> intRange(int first, int last) {
        return IntStream.range(first, last).boxed().toList();
    }

    public static List<Integer> intRangeClosed(int first, int last) {
        return IntStream.rangeClosed(first, last).boxed().toList();
    }
}

package at.woodexplosive.woodlib.impl.utils;

import java.util.List;

public final class CommandUtils {
    private CommandUtils() {}

    public static List<String> suggest(String typed, List<String> suggestions) {
        return typed.isBlank() ? suggestions : suggestions.stream().filter(s -> s.toLowerCase().startsWith(typed.toLowerCase())).toList();
    }

    public static List<String> suggest(String typed, String... suggestions) {
        return suggest(typed, List.of(suggestions));
    }
}

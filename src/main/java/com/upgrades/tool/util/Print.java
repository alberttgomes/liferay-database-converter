package com.upgrades.tool.util;

import java.time.Instant;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * @author Albett Gomes Cabral
 */
public class PrintLoggerUtil {

    public static void printInfo(String word) {
        _print(_GREEN, word);
    }

    public static void printError(String word, String ...cause) {
        if (cause == null) {
            _print(_RED, word);
        }
        else {
            _print(_RED, word + _BREAK_LINE + Arrays.toString(cause));
        }
    }

    public static void printReplacement(
            String oldContent, String newContent, Pattern pattern) {

        System.out.println("Applying pattern " + pattern.pattern());

        System.out.println(
                "Replace\n" + _LIGHT_BLUE + oldContent + _RESET +
                        _BREAK_LINE + "By\n" + _GREEN + newContent +
                            _RESET + _DOUBLE_BREAK_LINE);

    }

    public static void printWarning(String word, String cause) {
        if (cause == null) {
            _print(_YELLOW, word);
        }
        else {
            _print(_YELLOW, word + _BREAK_LINE +  cause);
        }
    }

    private static void _print(String color, String word) {
        System.out.println(
                Instant.now() + _STRING_WHITE_SPACE + color
                        + _STRING_WHITE_SPACE + word + _RESET);
    }

    // utilities variables

    private static final String _DOUBLE_BREAK_LINE = "\n\n";

    private static final String _BREAK_LINE = "\n";

    private static final String _STRING_WHITE_SPACE = " ";

    // colors variables

    private static final String _GREEN = "\u001B[32m";

    private static final String _LIGHT_BLUE = "\u001B[94m";

    private static final String _RED = "\u001B[31m";

    private static final String _RESET = "\u001B[0m";

    private static final String _YELLOW = "\u001B[33m";

}

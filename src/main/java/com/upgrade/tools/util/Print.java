package com.upgrade.tools.util;

import java.time.Instant;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * @author Albett Gomes Cabral
 */
public class Print {

    public static void info(String describe) {
        _print(_COLOR_GREEN, describe);
    }

    public static void error(String describe, String ...cause) {
        if (cause == null || cause.length == 0) {
            _print(_COLOR_RED, describe);
        }
        else {
            _print(_COLOR_RED, describe + _BREAK_LINE + Arrays.toString(cause));
        }
    }

    public static void replacement(
            String oldContent, String newContent, Pattern pattern) {

        System.out.println("Applying pattern " + pattern.pattern());

        System.out.println(
            "Replace\n" + _COLOR_LIGHT_BLUE + oldContent + _RESET +
                    _BREAK_LINE + "By\n" + _COLOR_GREEN + newContent +
                        _RESET + _DOUBLE_BREAK_LINE);
    }

    public static void warn(String describe, Exception cause) {
        if (cause == null) {
            _print(_COLOR_YELLOW, describe);
        }
        else {
            _print(_COLOR_YELLOW, describe + _BREAK_LINE +  cause);
        }
    }

    private static void _print(String color, String describe) {
        System.out.println(
            Instant.now() + _STRING_WHITE_SPACE + color
                + _STRING_WHITE_SPACE + describe + _RESET);
    }

    // utilities variables

    private static final String _DOUBLE_BREAK_LINE = "\n\n";

    private static final String _BREAK_LINE = "\n";

    private static final String _STRING_WHITE_SPACE = " ";

    // colors variables

    private static final String _COLOR_GREEN = "\u001B[32m";

    private static final String _COLOR_LIGHT_BLUE = "\u001B[94m";

    private static final String _COLOR_RED = "\u001B[31m";

    private static final String _COLOR_YELLOW = "\u001B[33m";

    private static final String _RESET = "\u001B[0m";

}

package com.upgrades.tool;

import com.upgrades.tool.convert.SchemeConverter;
import com.upgrades.tool.initialize.Initialize;
import com.upgrades.tool.util.Print;
import com.upgrades.tool.util.ResultsThreadLocal;

/**
 * @author Albert Gomes Cabral
 */
public class Main {

    public static void main(String[] args) throws Exception {

        SchemeConverter schemeConverter =
                Initialize.getConverterType(_DATABASE_TYPE);

        Print.info("Running %s".formatted(
                schemeConverter.getClass().getSimpleName()));

        schemeConverter.converter(
                _SOURCE_FILE_NAME, _TARGET_FILE_NAME, _NEW_FILE_NAME);

        if (ResultsThreadLocal.getResultsThreadLocal()) {
            Print.info("Converted with success.");
        }
        else {
            Print.error("Converter fail. Try again.");
        }

    }

    // Must be initialized

    private static final String _DATABASE_TYPE = "";

    private static final String _SOURCE_FILE_NAME = "";

    private static final String _TARGET_FILE_NAME = "";

    private static final String _NEW_FILE_NAME = "";

}
package com.upgrade.tools;

import com.upgrade.tools.convert.SchemeConverter;
import com.upgrade.tools.initialize.SchemeConverterInitialize;
import com.upgrade.tools.util.Print;
import com.upgrade.tools.util.ResultsThreadLocal;

/**
 * @author Albert Gomes Cabral
 */
public class Main {

    public static void main(String[] args) throws Exception {
        SchemeConverter schemeConverter = SchemeConverterInitialize.getConverterType(
                _DATABASE_TYPE);

        Print.info("Running " + schemeConverter.getClass().getSimpleName());

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
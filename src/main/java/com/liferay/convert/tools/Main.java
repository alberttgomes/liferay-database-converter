package com.liferay.convert.tools;

import com.liferay.convert.tools.migrate.ReplacementLiferayScheme;
import com.liferay.convert.tools.util.PrintLoggerUtil;
import com.liferay.convert.tools.util.ResultsThreadLocal;

/**
 * @author Albert Gomes Cabral
 */
public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println(
                "Converting liferay scheme types between Oracle and MySQL...");

        ReplacementLiferayScheme replacementLiferayScheme = new ReplacementLiferayScheme();

        replacementLiferayScheme.replacement(
                _SOURCE_FILE_NAME, _TARGET_FILE_NAME, _NEW_FILE_NAME);

        if (ResultsThreadLocal.getResultsThreadLocal()) {
            PrintLoggerUtil.printInfo(
                    "Replace between " + _SOURCE_FILE_NAME + " and " +
                            _TARGET_FILE_NAME + " to finished successfully.");
        }
        else {
            PrintLoggerUtil.printError(
                    "Replace fail. Try again!", null);
        }

    }

    // Necessary variables to initialize the app

    private static final String _SOURCE_FILE_NAME = "liferay-mysql-dump.sql";

    private static final String _TARGET_FILE_NAME = "customer_74_dump.sql";

    private static final String _NEW_FILE_NAME = "customer_74_new_dump.sql";

}
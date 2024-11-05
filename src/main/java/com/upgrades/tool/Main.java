package com.upgrades.tool;

import com.upgrades.tool.convert.ReplacementLiferayScheme;
import com.upgrades.tool.initialize.Initialize;
import com.upgrades.tool.util.PrintLoggerUtil;
import com.upgrades.tool.util.ResultsThreadLocal;

/**
 * @author Albert Gomes Cabral
 */
public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("Initializing App...");

        ReplacementLiferayScheme replacementLiferayScheme =
                Initialize.getReplacementType(_DATABASE_TYPE);

        replacementLiferayScheme.replacement(
                _SOURCE_FILE_NAME, _TARGET_FILE_NAME, _NEW_FILE_NAME);

        if (ResultsThreadLocal.getResultsThreadLocal()) {
            PrintLoggerUtil.printInfo(
                    "Replace between " + _SOURCE_FILE_NAME + " and " +
                            _TARGET_FILE_NAME + " to finished successfully.");
        }
        else {
            PrintLoggerUtil.printError(
                    "Replacement fail. Try again!");
        }

    }

    // Necessary variables to getReplacementType the app

    private static final String _DATABASE_TYPE = "mysql";

    private static final String _SOURCE_FILE_NAME = "liferay-mysql-dump.sql";

    private static final String _TARGET_FILE_NAME = "customer_74_dump_time_stamp.sql";

    private static final String _NEW_FILE_NAME = "customer_74_new_dump_time_stamp.sql";

}
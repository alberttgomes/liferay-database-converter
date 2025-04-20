package com.upgrade.tools;

import com.upgrade.tools.converter.SchemeConverter;
import com.upgrade.tools.initialize.SchemeConverterInitialize;
import com.upgrade.tools.util.Print;
import com.upgrade.tools.util.ResultsThreadLocal;

/**
 * @author Albert Gomes Cabral
 */
public class SchemeConverterMain {

    public static void main(String[] args) throws Exception {
        long start = System.nanoTime();

        Params params = _getParams(args);

        if (params == null) {
            throw new RuntimeException(
                "Is mandatory to inform valid arguments to use the converter. \n" +
                    "Use -h to see the usage.");
        }

        SchemeConverter schemeConverter = SchemeConverterInitialize.getConverterType(
            params.databaseType);

        Print.info("Running " + schemeConverter.getClass().getSimpleName());

        schemeConverter.converter(
            params.path, params.sourceFileName, params.targetFileName, params.newFileName);

        if (ResultsThreadLocal.getResultsThreadLocal()) {
            Print.info(
                "Converted with success. Completed in %d seconds"
                    .formatted(System.nanoTime() - start));
        }
        else {
            Print.error("Converter fail. Try again.");
        }
    }

    private static Params _getParams(String[] args) {
        if (args.length == 0) {
            return null;
        }

        Params params = new Params();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            switch (arg) {
                case "--database-type":
                case "-d":
                    params.databaseType = args[i + 1];
                    break;
                case "--path":
                case "-p":
                    params.path = args[i + 1];
                    break;
                case "--source-file":
                case "-sf":
                    params.sourceFileName = args[i + 1];
                    break;
                case "--target-file":
                case "-tf":
                    params.targetFileName = args[i + 1];
                    i++;
                    break;
                case "--new-file":
                case "-nf":
                    params.newFileName = args[i + 1];
                    break;
            }
        }

        return params;
    }

    private static class Params {

        public String databaseType;

        public String path;

        public String newFileName;

        public String sourceFileName;

        public String targetFileName;

    }

}
package com.upgrade.tools.executor;

import com.upgrade.tools.converter.SchemeConverter;
import com.upgrade.tools.initialize.SchemeConverterInitialize;

/**
 * @author Albert Gomes Cabral
 */
public class SchemeConverterExecutor {

    public static void executor(String[] args) throws Exception {
        Params params = _getParams(args);

        if (params == null) {
            throw new RuntimeException(
                "Is mandatory to inform valid arguments to use the converter. \n" +
                        "Use -h to see the usage.");
        }

        SchemeConverter schemeConverter = SchemeConverterInitialize.getConverterType(
                params.databaseType);

        schemeConverter.converter(
            params.path, params.sourceFileName, params.targetFileName, params.newFileName);
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

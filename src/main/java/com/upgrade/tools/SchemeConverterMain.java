package com.upgrade.tools;

import com.upgrade.tools.executor.SchemeConverterExecutor;
import com.upgrade.tools.util.Print;
import com.upgrade.tools.util.ResultsThreadLocal;

/**
 * @author Albert Gomes Cabral
 */
public class SchemeConverterMain {

    public static void main(String[] args) throws Exception {
        long start = System.nanoTime();

        SchemeConverterExecutor.executor(args);

        if (ResultsThreadLocal.getResultsThreadLocal()) {
            Print.info(
                "Converted with success. Completed in %d seconds"
                    .formatted(System.nanoTime() - start));
        }
        else {
            Print.error("Converter fail. Try again.");
        }
    }

}
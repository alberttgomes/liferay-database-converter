package com.liferay.convert.tools.util;

/**
 * @author Albert Gomes Cabral
 */
public class ResultsThreadLocal {

    public static Boolean getResultsThreadLocal() {
        return _resultsThreadLocal.get();
    }

    public static void setResultsThreadLocal(boolean result) {
        _resultsThreadLocal.set(result);
    }

    private static final ThreadLocal<Boolean> _resultsThreadLocal =
            ThreadLocal.withInitial(() -> false);

}

package com.upgrades.tool.convert;

import java.util.regex.Pattern;

/**
 * @author Albert Gomes Cabral
 */
public class SchemeMySQLConverter extends BaseConverter {

    @Override
    protected Pattern[] getContextPattern() {
        return new Pattern[]{_CONCAT_TABLE_NAME_PATTERN, _TABLE_NAME_PATTERN};
    }

    @Override
    protected String databaseType() {
        return "mysql";
    }

    private final Pattern _CONCAT_TABLE_NAME_PATTERN = Pattern.compile(
            "CREATE\\s+TABLE\\s+`(([A-Za-z]+)(_[a-zA-Z]+_)([0-9]+))`" +
                    "\\s*(\\((?:[^)(]+|\\([^)(]*\\))*\\))");

    private final Pattern _TABLE_NAME_PATTERN = Pattern.compile(
            "CREATE\\s+TABLE\\s+(`[A-z]+_?`)\\s*\\(((\\s*.*,)+(\\s*.*))\\s*\\)\"");

}
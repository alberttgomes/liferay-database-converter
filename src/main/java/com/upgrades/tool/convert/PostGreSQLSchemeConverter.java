package com.upgrades.tool.convert;

import java.util.regex.Pattern;

/**
 * @author Albert Gomes Cabral
 */
public class PostGreSQLSchemeConverter extends BaseConverter {

    @Override
    protected String databaseType() {
        return "postgresql";
    }

    @Override
    protected Pattern[] getContextPattern() {
        return new Pattern[] {_CONCAT_TABLE_NAME_PATTERN, _TABLE_NAME_PATTERN};
    }

    private final Pattern _CONCAT_TABLE_NAME_PATTERN = Pattern.compile(
            "CREATE\\s+TABLE\\s+public\\.(([A-Za-z]+)(_[a-zA-Z]+_)" +
                    "([0-9]+))\\s*\\((\\s*.*)+(\\s*.*)");

    private final Pattern _TABLE_NAME_PATTERN = Pattern.compile(
            "CREATE\\s+TABLE\\s+public\\.([A-z]+_?)\\s*\\((\\s*.*)+(\\s*.*)");

}

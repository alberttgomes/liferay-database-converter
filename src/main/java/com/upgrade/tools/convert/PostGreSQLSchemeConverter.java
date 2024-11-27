package com.upgrade.tools.convert;

import java.util.regex.Pattern;

/**
 * @author Albert Gomes Cabral
 */
public class PostGreSQLSchemeConverter extends BaseConverter {

    @Override
    protected Pattern[] getContextPattern() {
        return new Pattern[] {_TABLE_NAME_PATTERN};
    }

    @Override
    protected String getDatabaseType() {
        return "postgresql";
    }

    private final Pattern _TABLE_NAME_PATTERN = Pattern.compile(
            "CREATE\\s+TABLE\\s+(?:public\\.)?([a-zA-Z_]+)\\s*\\(([^)]*?(\\([^)]*\\)[^)]*?)*)\\);",
            Pattern.DOTALL
    );

}

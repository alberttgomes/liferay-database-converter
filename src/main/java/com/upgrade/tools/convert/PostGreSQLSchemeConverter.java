package com.upgrade.tools.convert;

import com.upgrade.tools.util.Print;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Albert Gomes Cabral
 */
public class PostGreSQLSchemeConverter extends BaseSchemeConverter {

    @Override
    protected Pattern[] getContextPattern() {
        return new Pattern[] {_TABLE_NAME_PATTERN};
    }

    @Override
    protected String getDatabaseType() {
        return "postgresql";
    }

    @Override
    protected String postProcess(String... content) {
        return _postgresqlConstraints(content[0], content[1]);
    }

    private String _postgresqlConstraints(
        String targetConstraints, String sourceConstraints) {

        Print.info(String.format(
            "Adding indexes and rules to %s", getDatabaseType()));

        Pattern indexesPattern = Pattern.compile(
            "CREATE\\s+INDEX\\s+(\\w+)\\s+ON\\s+public\\.(\\w+.*);");

        Matcher indexesMatcher = indexesPattern.matcher(sourceConstraints);

        String delimiter = "--\n" + "-- PostgreSQL database dump complete";

        while (indexesMatcher.find()) {
            targetConstraints = targetConstraints.replace(
                delimiter, indexesMatcher.group() + "\n\n" + delimiter
            );
        }

        Pattern uniqueIndexesPattern = Pattern.compile(
            "CREATE\\s+UNIQUE\\s+INDEX\\s+(\\w+)\\s+ON" +
                    "\\s+public\\.(\\w+.*);");

        Matcher uniqueIndexesMatcher =
            uniqueIndexesPattern.matcher(sourceConstraints);

        while (uniqueIndexesMatcher.find()) {
            targetConstraints = targetConstraints.replace(
                delimiter, uniqueIndexesMatcher.group() + "\n\n" + delimiter
            );
        }

        Pattern createRulesPattern = Pattern.compile(
            "CREATE\\s+RULE\\s+[\\w\\s]+ AS[\\s\\S]*?WHERE\\s*\\([^;]*\\);");

        Matcher createRulesMatcher = createRulesPattern.matcher(sourceConstraints);

        while (createRulesMatcher.find()) {
            targetConstraints = targetConstraints.replace(
                delimiter, createRulesMatcher.group() + "\n\n" + delimiter
            );
        }

        return targetConstraints;
    }

    private final Pattern _TABLE_NAME_PATTERN = Pattern.compile(
        "CREATE\\s+TABLE\\s+(?:public\\.)?([a-zA-Z_]+)\\s*\\(([^)]*?(\\([^)]*\\)[^)]*?)*)\\);",
        Pattern.DOTALL);

}

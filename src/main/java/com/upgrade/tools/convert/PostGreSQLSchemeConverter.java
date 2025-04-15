package com.upgrade.tools.convert;

import com.upgrade.tools.util.Print;

import java.util.ArrayList;
import java.util.List;
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
    protected List<String> postProcess(List<String> contents, String sourceContent) {
        return _postgresqlConstraints(contents, sourceContent);
    }

    private List<String> _postgresqlConstraints(
        List<String> targetStatements, String sourceStatement) {

        Print.info(String.format(
            "Executing post process to %s database", getDatabaseType()));

        List<String> resultStatements = new ArrayList<>();

        int index = 0;

        for (String statement : targetStatements) {
            index++;

            if (index == targetStatements.size()) {
                break;
            }

            Pattern copyStatementPattern = Pattern.compile(
                "COPY\\s*public\\.(\\w+)\\s+(\\(.*\\))\\s+FROM\\s+\\w+;");

            Matcher copyStatementMatcher = copyStatementPattern.matcher(statement);

            while (copyStatementMatcher.find()) {
                String tableName = copyStatementMatcher.group(1);

                if (sourceStatement.contains(tableName)) {
                    String copyStatement = copyStatementMatcher.group(2);

                    Print.replacement(
                        copyStatement, copyStatement.toLowerCase(), copyStatementPattern);

                    statement = statement.replace(
                        copyStatement, copyStatement.toLowerCase());
                }
            }

            resultStatements.add(statement);
        }

        String lastContentStatements = targetStatements.getLast();

        Pattern indexesPattern = Pattern.compile(
            "CREATE\\s+INDEX\\s+(\\w+)\\s+ON\\s+public\\.(\\w+.*);");

        Matcher indexesMatcher = indexesPattern.matcher(sourceStatement);

        String delimiter = "--\n" + "-- PostgreSQL database dump complete";

        while (indexesMatcher.find()) {
            lastContentStatements = lastContentStatements.replace(
                delimiter, indexesMatcher.group() + "\n\n" + delimiter
            );
        }

        Pattern uniqueIndexesPattern = Pattern.compile(
            "CREATE\\s+UNIQUE\\s+INDEX\\s+(\\w+)\\s+ON" +
                    "\\s+public\\.(\\w+.*);");

        Matcher uniqueIndexesMatcher =
            uniqueIndexesPattern.matcher(sourceStatement);

        while (uniqueIndexesMatcher.find()) {
            lastContentStatements = lastContentStatements.replace(
                delimiter, uniqueIndexesMatcher.group() + "\n\n" + delimiter
            );
        }

        Pattern createRulesPattern = Pattern.compile(
            "CREATE\\s+RULE\\s+[\\w\\s]+ AS[\\s\\S]*?WHERE\\s*\\([^;]*\\);");

        Matcher createRulesMatcher = createRulesPattern.matcher(sourceStatement);

        while (createRulesMatcher.find()) {
            lastContentStatements = lastContentStatements.replace(
                delimiter, createRulesMatcher.group() + "\n\n" + delimiter
            );
        }

        resultStatements.add(lastContentStatements);

        return resultStatements;
    }

    private final Pattern _TABLE_NAME_PATTERN = Pattern.compile(
        "CREATE\\s+TABLE\\s+(?:public\\.)?([a-zA-Z_0-9]+)\\s*\\(([^)]*?(\\([^)]*\\)[^)]*?)*)\\);",
        Pattern.DOTALL);

}

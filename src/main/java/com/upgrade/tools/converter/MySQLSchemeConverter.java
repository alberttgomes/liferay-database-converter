package com.upgrade.tools.converter;

import com.upgrade.tools.constants.SchemeConverterSupportType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Albert Gomes Cabral
 */
public class MySQLSchemeConverter extends BaseSchemeConverter {

    @Override
    protected String beforeProcess(String content, String sourceStatement) {
        return _mysqlConstraints(content, sourceStatement);
    }

    @Override
    protected Pattern[] getContextPattern() {
        return new Pattern[]{_CONCAT_TABLE_NAME_PATTERN, _TABLE_NAME_PATTERN};
    }

    @Override
    protected String getDatabaseType() {
        return SchemeConverterSupportType.MYSQL;
    }

    private String _mysqlConstraints(String columns, String constraints) {
        Pattern pattern = Pattern.compile("PRIMARY\\s+KEY\\s+(.+)(\\s*.*)+");

        Matcher matcher = pattern.matcher(constraints);

        StringBuilder sb = new StringBuilder();

        if (matcher.find()) {
            sb.append(columns);
            sb.append("\n");
            sb.append("  ");
            sb.append(matcher.group());
        }

        return sb.toString();
    }

    private final Pattern _CONCAT_TABLE_NAME_PATTERN = Pattern.compile(
        "CREATE\\s+TABLE\\s+`(([A-Za-z]+)(_[a-zA-Z]+_)([0-9]+))`" +
                "\\s*\\(((\\s*.*,)+(\\s*.*))\\s*\\)");

    private final Pattern _TABLE_NAME_PATTERN = Pattern.compile(
        "CREATE\\s+TABLE\\s+(`[A-z]+_?`)\\s*\\(((\\s*.*,)+(\\s*.*))\\s*\\)");

}

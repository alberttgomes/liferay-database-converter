package com.upgrades.tool.convert;

import java.util.regex.Pattern;

/**
 * @author Albert Gomes Cabral
 */
public class ReplacementLiferaySchemeMySQL extends BaseReplacement
        implements ReplacementLiferayScheme {

    @Override
    protected Pattern[] getContextPattern() {
        return new Pattern[]{
                Pattern.compile(
                        "CREATE\\s+TABLE\\s+(`[A-z]+_?`)\\s*\\(" +
                        "((\\s*.*,)+(\\s*.*))\\s*\\)\\s*ENGINE=InnoDB\\s*DEFAULT" +
                        "\\s*CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci?;"
                ),
                Pattern.compile(
                        "CREATE\\s+TABLE\\s+`(([A-Za-z]+)(_[a-zA-Z]+_)([0-9]+))`" +
                        "\\s*(\\((?:[^)(]+|\\([^)(]*\\))*\\))\\s*ENGINE=InnoDB\\s*DEFAULT\\s*" +
                        "CHARSET=utf8mb4\\s*COLLATE=utf8mb4_unicode_ci;"
                )
        };
    }

    @Override
    protected String getType() {
        return "mysql";
    }

    @Override
    public void replacement(
            String sourceFileName, String targetFileName, String newFileName)
        throws Exception {

        // Getting to super class

        super.replacement(sourceFileName, targetFileName, newFileName);
    }

}

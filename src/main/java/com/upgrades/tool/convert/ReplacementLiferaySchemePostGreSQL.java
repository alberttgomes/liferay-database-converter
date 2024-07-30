package com.upgrades.tool.convert;

import java.util.regex.Pattern;

/**
 * @author Albert Gomes Cabral
 */
public class ReplacementLiferaySchemePostGreSQL extends BaseReplacement
        implements ReplacementLiferayScheme {

    @Override
    protected Pattern[] getContextPattern() {
        return new Pattern[0];
    }

    @Override
    protected String getType() {
        return "postgresql";
    }

    @Override
    public void replacement(
            String sourceFileName, String targetFileName, String newFileName)
        throws Exception {

        // Calling to super class

        super.replacement(sourceFileName, targetFileName, newFileName);
    }

}

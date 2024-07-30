package com.upgrades.tool.convert;

/**
 * @author Albert Gomes Cabral
 */
public interface ReplacementLiferayScheme {

    void replacement(
            String sourceFileName, String targetFileName, String newFileName)
        throws Exception;

}

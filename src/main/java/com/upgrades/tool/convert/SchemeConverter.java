package com.upgrades.tool.convert;

/**
 * @author Albert Gomes Cabral
 */
public interface ConverterLiferayScheme {

    void converter(
            String sourceName, String targetName, String newName)
        throws Exception;

}

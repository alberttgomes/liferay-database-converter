package com.upgrade.tools.convert;

/**
 * @author Albert Gomes Cabral
 */
public interface SchemeConverter {

    void converter(
            String path, String sourceName, String targetName, String newName)
        throws Exception;

}

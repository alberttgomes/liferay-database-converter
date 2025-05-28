package com.upgrade.tools.converter;

import java.util.List;

/**
 * @author Albert Gomes Cabral
 */
public interface SchemeConverter {

    void converter(
            String path, String sourceName, String targetName, String newName, List<String> indexesName)
        throws Exception;

}

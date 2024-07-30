package com.upgrades.tool.initialize;

import com.upgrades.tool.convert.ReplacementLiferayScheme;
import com.upgrades.tool.convert.ReplacementLiferaySchemeMySQL;
import com.upgrades.tool.convert.ReplacementLiferaySchemePostGreSQL;
import com.upgrades.tool.exception.ReplacementException;

/**
 * @author Albert Gomes Cabral
 */
public class Initialize {

    public ReplacementLiferayScheme getReplacementType(
            String databaseType) throws ReplacementException {
        try {
            if (databaseType == null || databaseType.isEmpty()) {
                throw new ReplacementException(
                        "Database type cannot be null or empty." +
                                " Please provide a valid database type.");
            }

            // check database's type

            if (databaseType.equals("mysql")) {
                return new ReplacementLiferaySchemeMySQL();
            }
            else if (databaseType.equals("postgresql")) {
                return new ReplacementLiferaySchemePostGreSQL();
            }
            else {
                throw new ReplacementException(
                        "No database type supported: " + databaseType);
            }
        }
        catch (ReplacementException replacementException) {
            throw new ReplacementException(replacementException);
        }
    }

}

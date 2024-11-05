package com.upgrades.tool.initialize;

import com.upgrades.tool.constants.SupportedTypes;
import com.upgrades.tool.convert.ReplacementLiferayScheme;
import com.upgrades.tool.convert.ReplacementLiferaySchemeMySQL;
import com.upgrades.tool.convert.ReplacementLiferaySchemePostGreSQL;
import com.upgrades.tool.exception.DatabaseTypeException;

/**
 * @author Albert Gomes Cabral
 */
public class Initialize {

    public static ReplacementLiferayScheme getReplacementType(String databaseType)
        throws DatabaseTypeException {

        try {
            if (databaseType == null || databaseType.isEmpty()) {
                throw new DatabaseTypeException(
                        "Database type cannot be null or empty");
            }

            if (databaseType.equals(SupportedTypes.MYSQL)) {
                return new ReplacementLiferaySchemeMySQL();
            }
            else if (databaseType.equals(SupportedTypes.POSTGRES)) {
                return new ReplacementLiferaySchemePostGreSQL();
            }
            else {
                throw new DatabaseTypeException(
                        "No database type supported %s"
                                .formatted(databaseType));
            }
        }
        catch (DatabaseTypeException databaseTypeException) {
            throw new DatabaseTypeException(
                    databaseTypeException.getMessage());
        }

    }

}

package com.upgrade.tools.initialize;

import com.upgrade.tools.convert.MySQLSchemeConverter;
import com.upgrade.tools.convert.constants.SchemeConverterSupportedTypes;
import com.upgrade.tools.convert.PostGreSQLSchemeConverter;
import com.upgrade.tools.convert.SchemeConverter;
import com.upgrade.tools.exception.ConverterException;

/**
 * @author Albert Gomes Cabral
 */
public class SchemeConverterInitialize {

    public static SchemeConverter getConverterType
        (String databaseType) throws ConverterException {

        try {
            if (databaseType.isBlank()) {
                throw new ConverterException(
                        "Please provide a valid database type");
            }

            return switch (databaseType) {
                case SchemeConverterSupportedTypes.MYSQL ->
                    new MySQLSchemeConverter();
                case SchemeConverterSupportedTypes.POSTGRES ->
                    new PostGreSQLSchemeConverter();
                default ->
                    throw new ConverterException(
                        "Database type not supported " +
                                databaseType);
            };
        }
        catch (ConverterException converterException) {
            throw new ConverterException(converterException);
        }
    }

}
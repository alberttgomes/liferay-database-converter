package com.upgrade.tools.initialize;

import com.upgrade.tools.convert.MySQLSchemeConverter;
import com.upgrade.tools.convert.constants.SchemeConverterSupportType;
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
                case SchemeConverterSupportType.MYSQL ->
                    new MySQLSchemeConverter();
                case SchemeConverterSupportType.POSTGRES ->
                    new PostGreSQLSchemeConverter();
                default ->
                    throw new ConverterException(
                        "Database type not supported %s".formatted(
                                databaseType));
            };
        }
        catch (ConverterException converterException) {
            throw new ConverterException(converterException);
        }
    }

}
package com.upgrade.tools.initialize;

import com.upgrade.tools.convert.MySQLSchemeConverter;
import com.upgrade.tools.convert.PostGreSQLSchemeConverter;
import com.upgrade.tools.convert.SchemeConverter;
import com.upgrade.tools.exception.ConverterException;

import java.util.Objects;

/**
 * @author Albert Gomes Cabral
 */
public class Initialize {

    public static SchemeConverter getConverterType(String databaseType)
        throws ConverterException {

        try {
            if (Objects.isNull(databaseType)) {
                throw new ConverterException(
                        "Database type cannot be null or empty! " +
                                "Please provide a valid database type.");
            }

            return switch (databaseType) {
                case "mysql" ->
                        new MySQLSchemeConverter();
                case "postgresql" ->
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

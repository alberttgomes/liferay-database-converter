package com.upgrades.tool.initialize;

import com.upgrades.tool.convert.SchemeConverter;
import com.upgrades.tool.convert.MySQLSchemeConverter;
import com.upgrades.tool.convert.PostGreSQLSchemeConverter;
import com.upgrades.tool.exception.ConverterException;

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

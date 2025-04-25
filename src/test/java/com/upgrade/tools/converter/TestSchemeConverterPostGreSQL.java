package com.upgrade.tools.converter;

import com.upgrade.tools.exception.ConverterException;
import com.upgrade.tools.executor.SchemeConverterExecutor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Objects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Albert Gomes Cabral
 */
public class TestSchemeConverterPostGreSQL {

    @Test
    public void testSchemeConverterColumnDefinitions() throws Exception {
        String path = _basePath + "column-definitions/";

        SchemeConverterExecutor.executor(
            new String[]{
                "-d", "postgresql", "-p", path,
                "-sf", "source.sql", "-tf", "target.sql",
                "-nf", "new-create-table-statement.sql"
            });

        String newConvertedContent = _readContent(
            path, "new-create-table-statement.sql");

        String targetContent = _readContent(
            path, "excepted_create_table_statement.sql");

        Assertions.assertEquals(
            targetContent, newConvertedContent, "failed");
    }

    @Test
    public void testSchemeConverterParameters() throws Exception {
        String path = _basePath + "parameters/";

        // valid files extensions

        Assertions.assertThrows(
            ConverterException.class,
            () -> SchemeConverterExecutor.executor(
                    new String[]{
                        "-d", "postgresql", "-p", path,
                        "-sf", "source.bak", "-tf", "target.bak",
                        "-nf", "new-create-table-statement.sql"
                    })
        );

        // supported database parameter

        Assertions.assertThrows(
            ConverterException.class,
            () -> SchemeConverterExecutor.executor(
                    new String[]{
                        "-d", "mariadb", "-p", path,
                        "-sf", "source.sql", "-tf", "target.sql",
                        "-nf", "new-create-table-statement.sql"
                    })
        );

        // invalid path directory

        Assertions.assertThrows(
            ConverterException.class,
            () -> SchemeConverterExecutor.executor(
                    new String[]{
                        "-d", "mariadb", "-p", "/path",
                        "-sf", "source.sql", "-tf", "target.sql",
                        "-nf", "new-create-table-statement.sql"
                    })
        );
    }

    @AfterAll
    public static void cleanUp() {
        String path = _basePath + "column-definitions/";

        File file = new File(path + "new-create-table-statement.sql");

        System.out.printf("Clean up %s%n", file.delete());
    }

    private String _readContent(String path, String fileName) throws Exception {
        InputStream inputStream = new FileInputStream(path + fileName);

        return new String(
            Objects.requireNonNull(inputStream).readAllBytes());
    }

    private static final String _basePath =
        System.getProperty("user.dir") + "/src/test/resources/";

}

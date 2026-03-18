package com.upgrade.tools.converter;

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
    public void testWithCustomColumn() throws Exception {
        String path = _basePath + "column-definitions/postgresql/";

        String processed = "custom_column_test_processed.sql";

        SchemeConverterExecutor.executor(
            new String[]{
                "-d", "postgresql", "-p", path,
                "-sf", "custom_column_test_source.sql",
                "-tf", "custom_column_test_target.sql",
                "-nf", processed
            });

        Assertions.assertEquals(
            _readContent(path, processed),
            _readContent(path, "custom_column_test_expected.sql"));
    }

    @Test
    public void testWithReservedWordsColumn() throws Exception {
        String path = _basePath + "column-definitions/postgresql/";

        String processed = "reserved_words_column_test_processed.sql";

        SchemeConverterExecutor.executor(
            new String[]{
                "-d", "postgresql", "-p", path,
                "-sf", "reserved_words_column_test_source.sql",
                "-tf", "reserved_words_column_test_target.sql",
                "-nf", processed
            });

        Assertions.assertEquals(
            _readContent(path, processed),
            _readContent(
                path, "reserved_words_column_test_expected.sql"));
    }
    @Test
    public void testWithSortColumns() throws Exception {
        String path = _basePath + "column-definitions/postgresql/";

        String processed = "sort_column_test_processed.sql";

        SchemeConverterExecutor.executor(
            new String[]{
                "-d", "postgresql", "-p", path,
                "-sf", "sort_column_test_source.sql",
                "-tf", "sort_column_test_target.sql",
                "-nf", processed
            });

        Assertions.assertEquals(
            _readContent(path, processed),
            _readContent(
                path, "sort_column_test_expected.sql"));
    }

    @AfterAll
    public static void cleanUp() {
        String path = _basePath + "column-definitions/postgresql/";

        File customColumnFile = new File(
            path + "custom_column_test_processed.sql");

        if (customColumnFile.exists()) {
            System.out.printf(
                "Removing %s%n temp file", customColumnFile.delete());
        }

        File reservedWordsProcessedFile = new File(
            path + "reserved_words_column_test_processed.sql");

        if (reservedWordsProcessedFile.exists()) {
            System.out.printf(
                "Removing %s%n temp file",
                reservedWordsProcessedFile.delete());
        }

        File sortColumnFile = new File(
            path + "sort_column_test_processed.sql");

        if (sortColumnFile.exists()) {
            System.out.printf(
                "Removing %s%n temp file", sortColumnFile.delete());
        }
    }

    private String _readContent(String path, String fileName) throws Exception {
        try (InputStream inputStream = new FileInputStream(path + fileName)) {
            return new String(
                Objects.requireNonNull(inputStream).readAllBytes());
        }
        catch (Exception exception) {
            throw new Exception(exception);
        }
    }

    private static final String _basePath =
        System.getProperty("user.dir") + "/src/test/resources/";

}

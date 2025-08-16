package com.upgrade.tools.converter;

import com.upgrade.tools.exception.ConverterException;
import com.upgrade.tools.util.Print;
import com.upgrade.tools.util.ResultsThreadLocal;
import com.upgrade.tools.util.SchemeConverterUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Albert Gomes Cabral
 */
public abstract class BaseSchemeConverter implements SchemeConverter {

    protected abstract Pattern[] getContextPattern();

    protected abstract String getDatabaseType();

    protected String beforeProcess(
        String content, String sourceStatement) {

        return content;
    }

    protected List<String> postProcess(
        List<String> contents, String sourceContent, List<String> indexesName) {

        return contents;
    }

    @Override
    public void converter(
            String path, String sourceName, String targetName, String newName,
            List<String> indexesName)
        throws ConverterException {

        try {
            Print.info(
                "Starting scheme converter to %s database".formatted(
                    getDatabaseType()));

            Map<String, List<String>> contentsMap = _readFiles(
                path, sourceName, targetName);

            String sourceContent = contentsMap.get("source.content").getFirst();

            List<String> targetContentChunks = contentsMap.get("target.content");

            List<String> resultTargetContentChunks =
                new ArrayList<>(targetContentChunks.size());

            for (String targetContent : targetContentChunks) {
                for (Pattern pattern : getContextPattern()) {
                    targetContent = _converterContextPattern(
                        sourceContent, targetContent, pattern);

                    resultTargetContentChunks.add(targetContent);
                }
            }

            _writerResult(
                postProcess(resultTargetContentChunks, sourceContent, indexesName),
                path, newName);
        }
        catch (Exception exception) {
            throw new ConverterException(exception);
        }

    }

    private String _concat(String value, int index, int size) {
        if (index == size) {
            return value;
        }
        else {
            return value + ",";
        }
    }

    private String _converterContextPattern(
        String sourceContent, String targetContent, Pattern pattern) {

        Matcher matcherTarget = pattern.matcher(targetContent);

        while (matcherTarget.find()) {
            Matcher matcherSource = pattern.matcher(sourceContent);

            while (matcherSource.find()) {
                String tableNameSource = matcherSource.group(1);
                String tableNameTarget = matcherTarget.group(1);

                if (tableNameSource.equalsIgnoreCase(tableNameTarget)) {
                    Print.info(String.format("Converting %s table", tableNameSource));

                    targetContent = targetContent.replaceAll(
                        tableNameTarget, tableNameSource);

                    String columnsSource = matcherSource.group(2);
                    String columnsTarget = matcherTarget.group(2);

                    String convertedColumns = _getConvertedColumns(
                        columnsSource, columnsTarget);

                    targetContent = targetContent.replace(
                        columnsTarget, beforeProcess(convertedColumns, sourceContent));
                }
            }
        }

        return targetContent;
    }

    private String _extractColumnName(String column) {
        Pattern pattern = Pattern.compile("^`?\\w+`?");

        String normalizeColumn = column.replaceAll(
            "\"", "");

        Matcher matcher = pattern.matcher(normalizeColumn);

        return matcher.find() ? matcher.group() : null;
    }

    private String _formatColumns(Set<String> newColumns, String columnsTarget) {
        Pattern pattern = Pattern.compile(
            "(`?\"?[a-zA-Z0-9_.-]+_?\"?`?)\\s(\\w+\\(?.+),?");

        Matcher matcherTarget = pattern.matcher(columnsTarget);

        int index = 0;

        StringBuilder sb = new StringBuilder();

        while (matcherTarget.find()) {
            for (String column : newColumns) {
                Matcher matcherColumn = _COLUMN_NAME_PATTERN.matcher(column);

                while (matcherColumn.find()) {
                    String normalizeTargetColumn = matcherTarget.group(1).replaceAll(
                        "\"", "");
                    String normalizeNewColumn = matcherColumn.group(1).replaceAll(
                        "\"", "");

                    if (normalizeTargetColumn.equalsIgnoreCase(normalizeNewColumn)) {
                        index++;

                        matcherTarget.appendReplacement(
                            sb, Matcher.quoteReplacement(
                                    _concat(column, index, newColumns.size())));
                    }
                }
            }
        }

        matcherTarget.appendTail(sb);

        return sb.toString();
    }

    private String _getConvertedColumns(String sourceColumns, String targetColumns) {
        return _formatColumns(
            _newColumnsResults(sourceColumns, targetColumns),
            targetColumns);
    }

    private Set<String> _getColumnsSet(String columnContent) {
        Set<String> columns = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        for (String column : columnContent.split(",\\n")) {
            Matcher matcher = _COLUMN_NAME_PATTERN.matcher(column);

            if (matcher.find()) {
                columns.add(column.trim());
            }
        }

        return columns;
    }

    private Set<String> _newColumnsResults(String sourceColumns, String targetColumns) {
        Set<String> sourceColumnsSet = _getColumnsSet(sourceColumns);
        Set<String> targetColumnsSet = _getColumnsSet(targetColumns);

        Set<String> newColumns = new HashSet<>(sourceColumnsSet);

        targetColumnsSet.forEach(
            (column) -> {
                Matcher matcher = _COLUMN_NAME_PATTERN.matcher(column);

                if (matcher.find()) {
                    String columnTargetNormalized = matcher.group(1).replaceAll(
                        "\"", "").toLowerCase();

                    Set<String> collected = sourceColumnsSet.stream()
                        .map(this::_extractColumnName)
                        .filter(columnTargetNormalized::equalsIgnoreCase)
                        .collect(Collectors.toSet());

                    if (collected.contains(columnTargetNormalized)) return;

                    newColumns.add(column);
                }
            }
        );

        return newColumns;
    }

    private Map<String, List<String>> _readContentMap(
        InputStream source, InputStream target) throws IOException {

        Map<String, List<String>> contentMap = new HashMap<>();

        contentMap.put(
            "source.content",
            Collections.singletonList(
                SchemeConverterUtil.readContent(source)));

        contentMap.put(
            "target.content",
            SchemeConverterUtil.readChunks(target, 120000, Integer.MAX_VALUE));

        return contentMap;
    }

    private Map<String, List<String>> _readFiles(
        String path, String sourceName, String targetName) throws Exception {

        try {
            if (!sourceName.endsWith(_VALID_EXTENSION) || !targetName.endsWith(_VALID_EXTENSION)) {
                throw new Exception("File extension must ends with " + _VALID_EXTENSION);
            }

            InputStream sourceInputStream = new FileInputStream(path + sourceName);

            InputStream targetInputStream = new FileInputStream(path + targetName);

            if (sourceInputStream.read() <= 0 || targetInputStream.read() <= 0) {
                throw new Exception("File content cannot be empty");
            }

            return _readContentMap(sourceInputStream, targetInputStream);
        }
        catch (Exception exception) {
            throw new Exception(exception);
        }

    }

    private void _writerResult(
            List<String> contents, String path, String newName)
        throws IOException {

        File file = new File(path + newName);

        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(file))) {

            for (String content : contents) {
                writer.write(content);
            }

            ResultsThreadLocal.setResultsThreadLocal(true);
        }
        catch (IOException ioException) {
            throw new IOException(
                "Unable to create SQL output file" + ioException.getCause());
        }
    }

    private static final Pattern _COLUMN_NAME_PATTERN = Pattern.compile(
        "(`?\"?[a-zA-Z0-9_.-]+_?\"?`?)\\s+[^,]+(?:,|$)");

    private static final String _VALID_EXTENSION = ".sql";

}

package com.upgrade.tools.convert;

import com.upgrade.tools.convert.constants.SchemeConverterSupportedTypes;
import com.upgrade.tools.exception.ConverterException;
import com.upgrade.tools.util.Print;
import com.upgrade.tools.util.ResultsThreadLocal;
import com.upgrade.tools.util.SchemeConverterUtil;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Albert Gomes Cabral
 */
public abstract class BaseSchemeConverter implements SchemeConverter {

    protected abstract Pattern[] getContextPattern();

    protected abstract String getDatabaseType();

    protected List<String> postProcess(
        List<String> contents, String sourceContent) {

        return contents;
    }

    @Override
    public void converter(
            String sourceName, String targetName, String newName)
        throws ConverterException {

        try {
            List<Map<String, List<String>>> contentMapList = _readFiles(
                sourceName, targetName);

            String sourceContent = String.valueOf(
                contentMapList.getFirst().get("source.key"));

            List<String> targetContentChunks =
                contentMapList.get(1).get("target.key");

            List<String> resultTargetContentChunks =
                new ArrayList<>(targetContentChunks.size());

            for (String targetContent : targetContentChunks) {
                for (Pattern pattern : getContextPattern()) {
                    targetContent = converterContextPattern(
                        sourceContent, targetContent, pattern);

                    resultTargetContentChunks.add(targetContent);
                }
            }

            _writerResult(
                newName, postProcess(resultTargetContentChunks, sourceContent));
        }
        catch (Exception exception) {
            throw new ConverterException(exception);
        }

    }

    protected String converterContextPattern(
        String sourceContent, String targetContent, Pattern pattern) {

        Matcher matcherTarget = pattern.matcher(targetContent);

        while (matcherTarget.find()) {
            Matcher matcherSource = pattern.matcher(sourceContent);

            while (matcherSource.find()) {
                String tableNameSource = matcherSource.group(1);
                String tableNameTarget = matcherTarget.group(1);

                if (tableNameSource.equalsIgnoreCase(tableNameTarget)) {
                    Print.info(String.format("Converting table %s", tableNameSource));

                    targetContent = targetContent.replaceAll(tableNameTarget, tableNameSource);

                    String columnsSource = matcherSource.group(2);
                    String columnsTarget = matcherTarget.group(2);

                    String convertedColumns = _getColumns(columnsSource, columnsTarget);

                    targetContent = targetContent.replace(columnsTarget, convertedColumns);

                    if (getDatabaseType().equals(SchemeConverterSupportedTypes.MYSQL)) {
                        targetContent = _mysqlConstraints(convertedColumns, columnsSource);
                    }

                    Print.replacement(columnsTarget, convertedColumns, pattern);
                }
            }
        }

        return targetContent;
    }

    private Map<String, List<String>> _buildMapItem(
        String key, List<String> values) {

        Map<String, List<String>> itemMap = new HashMap<>();

        itemMap.put(key, values);

        return itemMap;
    }

    private String _concat(String value, int index, int size) {
        if (index == size) {
            return value;
        }
        else {
            return value + ",";
        }
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

    private String _getColumns(String sourceColumns, String targetColumns) {
        return _formatColumns(
            _verifyExistsCustomColumns(sourceColumns, targetColumns),
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

    private String _mysqlConstraints(String columns, String constraints) {
        Pattern pattern = Pattern.compile("PRIMARY\\s+KEY\\s+(.+)(\\s*.*)+");

        Matcher matcher = pattern.matcher(constraints);

        StringBuilder sb = new StringBuilder();

        if (matcher.find()) {
            sb.append(columns);
            sb.append("\n");
            sb.append("  ");
            sb.append(matcher.group());
        }

        return sb.toString();
    }

    private Map<String, List<String>> _readContentMap(
        InputStream source, InputStream target) throws IOException {

        Map<String, List<String>> contentMap = new HashMap<>();

        contentMap.put(
            "source.content",
            Collections.singletonList(
                SchemeConverterUtil.readContent(source)));

        // Avoid OutOfMemoryError reading large data as chunks mode
        contentMap.put(
            "target.content",
            SchemeConverterUtil.readChunks(target, 120000, Integer.MAX_VALUE));

        return contentMap;
    }

    private List<Map<String, List<String>>> _readFiles(String sourceName, String targetName)
        throws RuntimeException {

        try {
            if (!sourceName.endsWith(_VALID_EXTENSION) || !targetName.endsWith(_VALID_EXTENSION)) {
                throw new Exception("Extension file must ends " + _VALID_EXTENSION);
            }

            Thread thread = Thread.currentThread();

            ClassLoader classLoader = thread.getContextClassLoader();

            InputStream sourceInputStream = classLoader.getResourceAsStream(sourceName);

            InputStream targetInputStream = classLoader.getResourceAsStream(targetName);

            if (Objects.isNull(sourceInputStream)) {
                throw new RuntimeException("Source file not found " + sourceName);
            }
            else if (Objects.isNull(targetInputStream)) {
                throw new RuntimeException("Target file not found %s" +targetName);
            }
            else {
                Map<String, List<String>> contentsMap = _readContentMap(
                    sourceInputStream, targetInputStream);

                List<Map<String, List<String>>> contentMapList = new ArrayList<>(2);

                contentMapList.add(
                    _buildMapItem("source.key", contentsMap.get("source.content")));
                contentMapList.add(
                    _buildMapItem("target.key", contentsMap.get("target.content")));

                return contentMapList;
            }
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }

    }

    private Set<String> _verifyExistsCustomColumns(String sourceColumns, String targetColumns) {
        Set<String> sourceColumnsSet = _getColumnsSet(sourceColumns);
        Set<String> targetColumnsSet = _getColumnsSet(targetColumns);

        // Crete new columns based on source file
        Set<String> newColumns = new HashSet<>(sourceColumnsSet);

        // Check if existing custom columns to keep
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

    private void _writerResult(
        String newName, List<String> contents) throws IOException {

        String resourceDirectory = System.getProperty("user.dir") +
            "/src/main/resources/" + getDatabaseType() + "/";

        String filePath = resourceDirectory + newName;

        File file = new File(filePath);

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

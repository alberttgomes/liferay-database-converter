package com.upgrade.tools.convert;

import com.upgrade.tools.exception.ConverterException;
import com.upgrade.tools.util.Print;
import com.upgrade.tools.util.ResultsThreadLocal;
import com.upgrade.tools.util.SchemeConverterUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Albert Gomes Cabral
 */
public abstract class BaseConverter implements SchemeConverter {

    protected abstract Pattern[] getContextPattern();

    protected abstract String getDatabaseType();

    @Override
    public void converter(String sourceName, String targetName, String newName)
        throws ConverterException {

        try {
            List<Map<String, String>> contentMapList = _readFiles(
                    sourceName, targetName);

            String sourceContent = contentMapList.get(0).get("source.key");
            String targetContent = contentMapList.get(1).get("target.key");

            String resultContent = targetContent;

            for (Pattern pattern : getContextPattern()) {
                resultContent = converterContextPattern(
                        sourceContent, resultContent, pattern);
            }

            if (resultContent.equals(targetContent)) {
                Print.warn("No exchanges were recorded", null);
            }
            else {
                if (getDatabaseType().equals("postgresql")) {
                    Print.info("Adding indexes and rules to %s".formatted(
                            getDatabaseType()));

                    resultContent = _postgresqlConstraints(
                            resultContent, sourceContent);
                }

                _writerResult(newName, resultContent);
            }
        }
        catch (Exception exception) {
            throw new ConverterException(exception);
        }

    }

    protected String converterContextPattern(
            String sourceContent, String targetContent, Pattern pattern)
        throws ConverterException {

        try {
            Matcher matcherTarget = pattern.matcher(targetContent);

            while (matcherTarget.find()) {
                Matcher matcherSource = pattern.matcher(sourceContent);

                while (matcherSource.find()) {
                    String tableNameSource = matcherSource.group(1);
                    String tableNameTarget = matcherTarget.group(1);

                    if (tableNameSource.equalsIgnoreCase(tableNameTarget)) {
                        Print.info("Updating table %s".formatted(tableNameSource));

                        targetContent = targetContent.replaceAll(
                                tableNameTarget, tableNameSource);

                        String columnsSource = matcherSource.group(2);
                        String columnsTarget = matcherTarget.group(2);

                        String convertedColumns = _getColumns(
                                columnsSource, columnsTarget);

                        targetContent = targetContent.replace(
                                columnsTarget, convertedColumns);

                        if (getDatabaseType().equals("mysql")) {
                            targetContent = _mysqlConstraints(
                                    convertedColumns, columnsSource);
                        }

                        Print.replacement(
                                columnsTarget, convertedColumns, pattern);
                    }
                }
            }
            return targetContent;
        }
        catch (Exception exception) {
            throw new ConverterException(exception);
        }
    }

    private Map<String, String> _buildMapItem(String key, String value) {
        Map<String, String> itemMap = new HashMap<>();

        itemMap.put(key, value);

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
        Matcher matcher = pattern.matcher(column);

        return matcher.find() ? matcher.group() : null;
    }

    private String _formatColumns(Set<String> newColumns, String columnsTarget) {
        Pattern pattern = Pattern.compile(
                "(`?\"?[a-zA-Z0-9_.-]+_?\"?`?)\\s(\\w+\\(?.+),?");

        Matcher matcherTarget = pattern.matcher(columnsTarget);

        int index = 0;

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

                        columnsTarget = columnsTarget.replace(
                                matcherTarget.group(),
                                _concat(column, index, newColumns.size()));
                    }
                }
            }
        }

        return columnsTarget;
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

    private String _postgresqlConstraints(String targetConstraints, String sourceConstraints) {
        Pattern indexesPattern = Pattern.compile(
                "CREATE\\s+INDEX\\s+(\\w+)\\s+ON\\s+public\\.(\\w+.*);");

        Matcher indexesMatcher = indexesPattern.matcher(sourceConstraints);

        String delimiter = "--\n" + "-- PostgreSQL database dump complete";

        while (indexesMatcher.find()) {
            targetConstraints = targetConstraints.replace(
                    delimiter,
                    indexesMatcher.group() + "\n\n" + delimiter
            );
        }

        Pattern uniqueIndexesPattern = Pattern.compile(
                "CREATE\\s+UNIQUE\\s+INDEX\\s+(\\w+)\\s+ON" +
                        "\\s+public\\.(\\w+.*);");

        Matcher uniqueIndexesMatcher = uniqueIndexesPattern.matcher(sourceConstraints);

        while (uniqueIndexesMatcher.find()) {
            targetConstraints = targetConstraints.replace(
                    delimiter,
                    uniqueIndexesMatcher.group() + "\n\n" + delimiter
            );
        }

        Pattern createRulePattern = Pattern.compile(
                "CREATE\\s+RULE\\s+[\\w\\s]+ AS[\\s\\S]*?WHERE\\s*\\([^;]*\\);");

        Matcher createRuleMatcher = createRulePattern.matcher(sourceConstraints);

        while (createRuleMatcher.find()) {
            targetConstraints = targetConstraints.replace(
                    delimiter,
                    createRuleMatcher.group() + "\n\n" + delimiter
            );
        }

        return targetConstraints;
    }

    private Map<String, String> _readContentMap(
            InputStream source, InputStream target) throws IOException {

        Map<String, String> contentMap = new HashMap<>();

        contentMap.put("source.content", SchemeConverterUtil.readContent(source));
        contentMap.put("target.content", SchemeConverterUtil.readContent(target));

        return contentMap;
    }

    private List<Map<String, String>> _readFiles(String sourceName, String targetName)
        throws RuntimeException {

        try {
            if (!sourceName.endsWith(_VALID_EXTENSION) || !targetName.endsWith(_VALID_EXTENSION)) {
                throw new Exception("Extension file must ends %s".formatted(_VALID_EXTENSION));
            }

            Thread thread = Thread.currentThread();

            ClassLoader classLoader = thread.getContextClassLoader();

            InputStream sourceInputStream = classLoader.getResourceAsStream(sourceName);

            InputStream targetInputStream = classLoader.getResourceAsStream(targetName);

            if (Objects.isNull(sourceInputStream)) {
                throw new RuntimeException(
                        "Source file not found %s".formatted(sourceName));
            }
            else if (Objects.isNull(targetInputStream)) {
                throw new RuntimeException(
                        "Target file not found %s".formatted(targetName));
            }
            else {
                Map<String, String> contentsMap = _readContentMap(
                        sourceInputStream, targetInputStream);

                List<Map<String, String>> contentMapList = new ArrayList<>(2);

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
                        String columnTarget = matcher.group(1);

                        Set<String> collected = sourceColumnsSet.stream()
                                .map(this::_extractColumnName)
                                .filter(columnTarget::equalsIgnoreCase)
                                .collect(Collectors.toSet());

                        if (collected.contains(columnTarget)) return;

                        newColumns.add(column);
                    }
                }
        );

        return newColumns;
    }

    private void _writerResult(String newName, String content) throws IOException {
        String resourceDirectory = System.getProperty("user.dir") +
                "/src/main/resources/" + getDatabaseType() + "/";

        String filePath = resourceDirectory + newName;

        File file = new File(filePath);

        try (FileWriter writer = new FileWriter(file)) {
            try {
                if (file.exists()) {
                    writer.write(content);

                    ResultsThreadLocal.setResultsThreadLocal(true);
                }
                else {
                    throw new IOException(
                            "File already exists %s".formatted(newName));
                }
            }
            catch (Exception exception) {
                throw new IOException(
                        "Unable to create SQL output file %s".formatted(
                                exception.getCause()));
            }
            finally {
                writer.flush();

                writer.close();
            }
        }
        catch (IOException ioException) {
            throw new IOException(ioException);
        }
    }

    private static final Pattern _COLUMN_NAME_PATTERN = Pattern.compile(
            "(`?\"?[a-zA-Z0-9_.-]+_?\"?`?)\\s+[^,]+(?:,|$)");

    private static final String _VALID_EXTENSION = ".sql";

}

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

            // Writer out put file

            _writerResult(newName, resultContent);
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
                        //  Replace all occurrences to table name
                        targetContent = targetContent.replaceAll(
                                tableNameTarget, tableNameSource);

                        Print.replacement(
                                tableNameTarget, tableNameSource, pattern);

                        // Replace columns' definitions
                        String columnsSource = matcherSource.group(2);
                        String columnsTarget = matcherTarget.group(2);

                        String convertedColumns =
                                _getColumns(columnsSource, columnsTarget);

                        targetContent = targetContent.replace(
                                columnsTarget, convertedColumns);

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

    private void _writerResult(String newName, String content) throws IOException {
        String resourceDirectory = System.getProperty("user.dir") +
                "/src/main/resources/" + getDatabaseType();

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

    private String _formatColumns(
            Set<String> newColumns, String columnsTarget, String columnsSource) {

        Pattern pattern = Pattern.compile("(`\\w+`)\\s(\\w+\\(?.+),?");

        Matcher matcher = pattern.matcher(columnsTarget);

        while (matcher.find()) {
            for (String column : newColumns) {
                Matcher matcher1 = _COLUMN_NAME_PATTERN.matcher(column);

                while (matcher1.find()) {
                    if (matcher.group(1).equalsIgnoreCase(
                            matcher1.group(1))) {

                        columnsTarget = columnsTarget.replace(
                                matcher.group(), column + ",");
                    }
                }
            }
        }

        return _getConstraints(columnsTarget, columnsSource);
    }

    private String _getColumns(String sourceColumns, String targetColumns) {
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

                        if (!sourceColumnsSet.toString().contains(columnTarget)) {
                            newColumns.add(column);
                        }
                    }
                }
        );

        return _formatColumns(newColumns, targetColumns, sourceColumns);
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

    private String _getConstraints(String columns, String constraints) {
        return switch (getDatabaseType()) {
            case "postgresql" ->
                    _postgresqlConstraints(columns, constraints);
            case "mysql" ->
                    _mysqlConstraints(columns, constraints);
            default -> columns;
        };
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

    private String _postgresqlConstraints(String columns, String constraints) {
        Pattern alterTablePattern = Pattern.compile(
                "ALTER\\s+TABLE\\s+ONLY\\s+public\\.(\\w+)\\" +
                        "s+ADD\\s+CONSTRAINT\\s+\\w+\\s+PRIMARY\\s+KEY\\s+(.+)");

        Matcher matcher = alterTablePattern.matcher(constraints);

        if (matcher.find()) {
            columns = columns.concat(matcher.group());

            Pattern indexesPattern = Pattern.compile(
                    "CREATE\\s+INDEX\\s+(\\w+)\\s+ON\\s+public\\.(\\w+.*);");

            Matcher indexesMatcher = indexesPattern.matcher(constraints);

            if (indexesMatcher.find()) {
                columns = columns.concat(indexesMatcher.group());

                Pattern uniqueIndexesPattern = Pattern.compile(
                        "CREATE\\s+UNIQUE\\s+INDEX\\s+(\\w+)\\s+ON" +
                                "\\s+public\\.(\\w+.*);");

                Matcher uniqueIndexesMatcher =
                        uniqueIndexesPattern.matcher(constraints);

                if (uniqueIndexesMatcher.find()) {
                    return columns.concat(uniqueIndexesMatcher.group());
                }
            }
        }

        return columns;
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

    private static final Pattern _COLUMN_NAME_PATTERN = Pattern.compile(
            "(`?[A-z]+_?`?)\\s+[^,]+(?:,|$)");

    private static final String _VALID_EXTENSION = ".sql";

}

package com.upgrade.tools.convert;

import com.upgrade.tools.exception.ConverterException;
import com.upgrade.tools.util.Print;
import com.upgrade.tools.util.ResultsThreadLocal;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Albert Gomes Cabral
 */
public abstract class BaseConverter implements SchemeConverter {

    protected abstract String databaseType();

    protected abstract Pattern[] getContextPattern();

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
                resultContent = _converterContextPattern(
                        sourceContent, resultContent, pattern);
            }

            if (resultContent.equals(targetContent)) {
                throw new ConverterException("No exchanges were recorded");
            }

            // Writer out put file

            _writerResult(newName, resultContent);
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

    private String _converterContextPattern(
            String sourceContent, String targetContent, Pattern pattern)
        throws ConverterException {

        try {
            Matcher matcherTarget = pattern.matcher(targetContent);

            while (matcherTarget.find()) {
                Matcher matcherSource = pattern.matcher(sourceContent);

                while (matcherSource.find()) {
                    String patternDefinition = pattern.toString();

                    if (patternDefinition.contains("(([A-Za-z]+)(_[a-zA-Z]+_)([0-9]+))")) {

                        if (matcherTarget.group(2).equalsIgnoreCase(matcherSource.group(2))) {
                            String name = matcherSource.group(2);
                            String groupId = matcherTarget.group(4);
                            String concatGroupId = name + matcherSource.group(3) + groupId;

                            // Replace table name concat with group id

                            targetContent = targetContent.replace(
                                    matcherTarget.group(1), concatGroupId);

                            Print.replacement(
                                    matcherTarget.group(1), concatGroupId, pattern);

                            // Replace table definitions

                            targetContent = targetContent.replace(
                                    matcherTarget.group(5), matcherSource.group(5));

                            Print.replacement(
                                    matcherTarget.group(5), matcherSource.group(5), pattern);
                        }
                    }
                    else {

                        if (matcherTarget.group(1).equalsIgnoreCase(matcherSource.group(1))) {

                            // Replace table name

                            targetContent = targetContent.replace(
                                    matcherTarget.group(1), matcherSource.group(1));

                            Print.replacement(
                                    matcherTarget.group(1), matcherSource.group(1), pattern);

                            // Replace table tableDefinitions

                            String definitionsSource = matcherSource.group(2);
                            String definitionsTarget = matcherTarget.group(2);

                            String tableDefinitions = _getColumns(
                                    definitionsSource, definitionsTarget);

                            targetContent = targetContent.replace(
                                    definitionsTarget, tableDefinitions);

                            Print.replacement(
                                    definitionsTarget, tableDefinitions, pattern);
                        }
                    }
                }
            }

            return targetContent;
        }
        catch (Exception exception) {
            throw new ConverterException(exception);
        }
    }

    private void _writerResult(String newName, String content) throws IOException {
        String resourceDirectory = System.getProperty("user.dir") +
                _RESOURCE_DIRECTORY;

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
                        "Unable to create SQL output file " +
                                exception.getCause());
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
            Set<String> allSourceColumn, String columnTarget, String columnSource) {

        Pattern pattern = Pattern.compile("(`\\w+`)\\s(\\w+\\(?.+),?");

        Matcher matcher = pattern.matcher(columnTarget);

        while (matcher.find()) {
            for (String colum : allSourceColumn) {
                Matcher matcher1 = _COLUMN_NAME_PATTERN.matcher(colum);

                while (matcher1.find()) {
                    if (matcher.group(1).equalsIgnoreCase(
                            matcher1.group(1))) {

                        columnTarget = columnTarget.replace(
                                matcher.group(), colum + ",");
                    }
                }
            }
        }

        return _getConstraints(columnTarget, columnSource);
    }

    private String _getColumns(String sourceColumns, String targetColumns) {
        Set<String> columnsTargetSet = _getColumnsSet(targetColumns);
        Set<String> columnsSourceSet = _getColumnsSet(sourceColumns);

        // Check if exist custom column to keep

        columnsTargetSet.forEach(
                (column) -> {
                    Matcher matcher = _COLUMN_NAME_PATTERN.matcher(column);

                    if (matcher.find()) {
                        String columnTarget = matcher.group(1);

                        if (!columnsSourceSet.toString().contains(columnTarget)) {
                            columnsSourceSet.add(column);
                        }
                    }
                }
        );

        return _formatColumns(columnsSourceSet, targetColumns, sourceColumns);
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

    private String _readContent(InputStream inputStream) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream))) {

            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            return stringBuilder.toString();
        }
    }

    private Map<String, String> _readContentMap(
            InputStream source, InputStream target) throws IOException {

        Map<String, String> contentMap = new HashMap<>();

        contentMap.put("source.content", _readContent(source));
        contentMap.put("target.content", _readContent(target));

        return contentMap;
    }

    private List<Map<String, String>> _readFiles(String sourceName, String targetName)
        throws RuntimeException {

        try {
            if (!sourceName.endsWith(_VALID_EXTENSION) && !targetName.endsWith(_VALID_EXTENSION)) {
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
            "(`[A-z]+_?`)\\s+[^,]+(?:,|$)");

    private static final String _RESOURCE_DIRECTORY = "/src/main/resources/";

    private static final String _VALID_EXTENSION = ".sql";

}

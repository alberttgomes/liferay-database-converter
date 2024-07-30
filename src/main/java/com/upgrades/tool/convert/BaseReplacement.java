package com.upgrades.tool.convert;

import com.upgrades.tool.exception.ReplacementException;
import com.upgrades.tool.exception.SQLFilesException;
import com.upgrades.tool.util.PrintLoggerUtil;
import com.upgrades.tool.util.ResultsThreadLocal;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Albert Gomes Cabral
 */
public abstract class BaseReplacement {

    protected abstract Pattern[] getContextPattern();

    protected abstract String getType();

    // Loads files put on resources directory and getting the replacementContextPatter method.
    // Also, getting the _createFileResult method.

    /**
     *
     * @param sourceFileName file's name to the source (Source Dump)
     * @param targetFileName  file's name to the target (Target Dump)
     * @param newFileName file's name to the file output
     * @throws Exception throws exception if some error occur
     */
    protected void replacement(
            String sourceFileName, String targetFileName, String newFileName)
        throws Exception {
        try {
            if (sourceFileName == null || sourceFileName.isBlank()
                    || targetFileName == null || targetFileName.isBlank()
                    || newFileName == null || newFileName.isBlank()) {

                PrintLoggerUtil.printError("Invalid params");

                return;
            }

            List<Map<String, String>> contentMapList = _getContentFromFile(
                    sourceFileName, targetFileName);

            if (contentMapList != null && contentMapList.size() == 2) {

                String sourceContent = contentMapList.get(0).get("source.key");
                String targetContent = contentMapList.get(1).get("target.key");

                String firstContentTarget = targetContent;

                if (sourceContent != null && targetContent != null) {
                    for (Pattern pattern : getContextPattern()) {
                        targetContent = replacementContextPattern(
                                sourceContent, targetContent, pattern, sourceFileName);
                    }

                    if (firstContentTarget.equals(targetContent)) {
                        throw new ReplacementException("No exchanges were recorded");
                    }

                    // Create output file results

                    _createFileResult(newFileName, targetContent);
                }
                else {
                    throw new ReplacementException(
                            "Source or Target files produced null pointer.");
                }
            }
            else {
                throw new Exception("Map content list is invalid");
            }
        }
        catch (Exception exception) {
            throw new Exception("Unable to replace contents ", exception);
        }
    }

    protected String replacementContextPattern(
            String sourceContent, String targetContent, Pattern pattern, String sourceFileName)
        throws ReplacementException {
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

                            PrintLoggerUtil.printReplacement(
                                    matcherTarget.group(1), concatGroupId, pattern);

                            // Replace table definitions

                            targetContent = targetContent.replace(
                                    matcherTarget.group(5), matcherSource.group(5));

                            PrintLoggerUtil.printReplacement(
                                    matcherTarget.group(5), matcherSource.group(5), pattern);
                        }
                    }
                    else {
                        if (matcherTarget.group(1).equalsIgnoreCase(matcherSource.group(1))) {

                            // Replace table name

                            targetContent = targetContent.replace(
                                    matcherTarget.group(1), matcherSource.group(1));

                            PrintLoggerUtil.printReplacement(
                                    matcherTarget.group(1), matcherSource.group(1), pattern);

                            // Replace table tableDefinitions

                            String definitionsSource = matcherSource.group(2);
                            String definitionsTarget = matcherTarget.group(2);

                            String tableDefinitions = _getColumns(
                                    definitionsSource, definitionsTarget);

                            targetContent = targetContent.replace(
                                    definitionsTarget, tableDefinitions);

                            PrintLoggerUtil.printReplacement(
                                    definitionsTarget, tableDefinitions, pattern);
                        }
                    }
                }
            }

            return targetContent;
        }
        catch (Exception exception) {
            throw new ReplacementException(
                    "Cannot replacement content from the file " + sourceFileName,
                        exception);
        }
    }

    private Map<String, String> _buildMapItem(String key, String value) {
        Map<String, String> itemMap = new HashMap<>();

        itemMap.put(key, value);

        return itemMap;
    }

    private void _createFileResult(
            String newFileName, String content) throws IOException {

        String resourceDirectory = System.getProperty("user.dir") +
                _RESOURCE_DIRECTORY;

        String filePath = resourceDirectory + newFileName;

        File file = new File(filePath);

        try (FileWriter writer = new FileWriter(file)) {
            try {
                if (file.exists()) {
                    writer.write(content);

                    ResultsThreadLocal.setResultsThreadLocal(true);

                    PrintLoggerUtil.printInfo("The " + file.getName() +
                            " was create with success.");
                }
                else {
                    throw new IOException(
                            "File with the name " + newFileName +
                                    " already exists.");
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
            Set<String> columnsSource, String columnContentTarget, String columnContentSource) {

        if (columnsSource == null || columnsSource.isEmpty()) {
            return null;
        }

        Pattern pattern = Pattern.compile("(`\\w+`)\\s(\\w+\\(?.+),?");

        Matcher matcher = pattern.matcher(columnContentTarget);

        while (matcher.find()) {
            for (String colum : columnsSource) {
                Matcher matcher1 = _COLUMN_NAME_PATTERN.matcher(colum);

                while (matcher1.find()) {
                    if (matcher.group(1).equalsIgnoreCase(
                            matcher1.group(1))) {

                        columnContentTarget = columnContentTarget.replace(
                                matcher.group(), colum + ",");
                    }
                }
            }
        }

        return _getConstraints(columnContentTarget, columnContentSource);
    }

    private String _getColumns(String sourceColumns, String targetColumns) {
        Set<String> columnsTargetSet = _getColumnsSet(targetColumns, false);
        Set<String> columnsSourceSet = _getColumnsSet(sourceColumns, false);

        // Getting name columns from source data

        Set<String> onlyColumnsName = _getColumnsSet(sourceColumns, true);

        // Check if exist custom columns to keeping

        columnsTargetSet.forEach(
                (column) -> {
                    Matcher matcher1 = _COLUMN_NAME_PATTERN.matcher(column);

                    if (matcher1.find()) {
                        String columnTarget = matcher1.group(1);

                        if (!onlyColumnsName.contains(columnTarget)) {
                            columnsSourceSet.add(column);
                        }
                    }
                }
        );

        return _formatColumns(columnsSourceSet, targetColumns, sourceColumns);
    }

    private Set<String> _getColumnsSet(String contentField, boolean onlyColumnName) {
        Set<String> fields = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        for (String column : contentField.split(",\\n")) {
            Matcher matcher = _COLUMN_NAME_PATTERN.matcher(column);

            if (matcher.find()) {
                if (onlyColumnName) {
                    fields.add(matcher.group(1));
                }
                else {
                    fields.add(column.trim());
                }
            }
        }

        return fields;
    }

    private String _getConstraints(String columns, String constraints) {
        Pattern pattern = Pattern.compile(
                "PRIMARY\\s+KEY\\s+(.+)(\\s*.*)+");

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

    private List<Map<String, String>> _getContentFromFile(
            String sourceFileName, String targetFileName) throws SQLFilesException {
        try {
            if (!sourceFileName.endsWith(".sql") && targetFileName.endsWith(".sql")) {
                throw new SQLFilesException("Extension file must be .sql");
            }

            Thread thread = Thread.currentThread();

            ClassLoader classLoader = thread.getContextClassLoader();

            InputStream sourceInputStream =
                    classLoader.getResourceAsStream(sourceFileName);

            InputStream targetInputStream =
                    classLoader.getResourceAsStream(targetFileName);

            if (sourceInputStream == null) {
                throw new SQLFilesException(
                        "Source file with the name " + sourceFileName +
                                " not found.");
            }
            else if (targetInputStream == null) {
                throw new SQLFilesException(
                        "Target file with the name " + targetFileName +
                                " not found.");
            }
            else {
                String source = _getContentInputStream(sourceInputStream);
                String target = _getContentInputStream(targetInputStream);

                if (source.isEmpty() && target.isEmpty() ||
                        source.isBlank() && target.isBlank()) {

                    PrintLoggerUtil.printError(
                            "Cannot convert input stream to string! " +
                                    "Invalid input file.");

                    return null;
                }

                List<Map<String, String>> contentMapList = new ArrayList<>(2);

                contentMapList.add(_buildMapItem("source.key", source));
                contentMapList.add(_buildMapItem("target.key", target));

                return contentMapList;
            }
        }
        catch (Exception exception) {
            throw new SQLFilesException("Unable to load files ", exception);
        }

    }

    private String _getContentInputStream(InputStream inputStream) throws IOException {
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

    // Patterns variables

    private static final Pattern _COLUMN_NAME_PATTERN = Pattern.compile(
            "(`[A-z]+_?`)\\s+[^,]+(?:,|$)");

    // Utilities variables

    private static final String _RESOURCE_DIRECTORY = "/src/main/resources/";

}

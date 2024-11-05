package com.upgrades.tool.convert;

import com.upgrades.tool.constants.SupportedTypes;
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

    protected void replacement(
            String sourceFileName, String targetFileName, String newFileName)
        throws Exception {

        try {
            if (Objects.isNull(sourceFileName) && Objects.isNull(targetFileName)) {
                PrintLoggerUtil.printError("Invalid params");

                return;
            }

            List<Map<String, String>> contentMapList =
                    _getContentResourceDirectory(sourceFileName, targetFileName);

            if (contentMapList != null && contentMapList.size() == 2) {
                String sourceContent = contentMapList.get(0).get("source.key");
                String targetContent = contentMapList.get(1).get("target.key");

                String originalContentTarget = targetContent;

                if (sourceContent != null && targetContent != null) {
                    for (Pattern pattern : getContextPattern()) {
                        targetContent = replacementContextPattern(
                                sourceContent, targetContent, pattern, sourceFileName);
                    }

                    if (originalContentTarget.equals(targetContent)) {
                        throw new ReplacementException(
                                "No exchanges were recorded");
                    }

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
            throw new Exception(exception.getCause());
        }
    }

    protected String replacementContextPattern(
            String sourceContent, String targetContent, Pattern pattern,
            String sourceFileName)
        throws ReplacementException {

        try {
            Matcher matcherTarget = pattern.matcher(targetContent);

            while (matcherTarget.find()) {
                Matcher matcherSource = pattern.matcher(sourceContent);

                while (matcherSource.find()) {
                    String patternDefinition = pattern.toString();
                    String specialCharacters = "(([A-Za-z]+)(_[a-zA-Z]+_)([0-9]+))";

                    if (patternDefinition.contains(specialCharacters)) {
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

                            // Replacing the columns and constraints

                            String fixedDefinitions = _getColumns(
                                    definitionsSource, definitionsTarget);

                            targetContent = targetContent.replace(
                                    definitionsTarget, fixedDefinitions);

                            PrintLoggerUtil.printReplacement(
                                    definitionsTarget, fixedDefinitions, pattern);
                        }
                    }
                }
            }

            return targetContent;
        }
        catch (Exception exception) {
            throw new ReplacementException(
                    "Cannot replacement content from the file %s"
                            .formatted(sourceFileName), exception);
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
            Set<String> columnsSourceSet, String columnsTarget, String columnsSource)
        throws ReplacementException {

        if (!Objects.nonNull(columnsSourceSet)) {
            return null;
        }

        Pattern pattern = Pattern.compile("(`\\w+`)\\s(\\w+\\(?.+),?");

        Matcher matcher = pattern.matcher(columnsTarget);

        while (matcher.find()) {
            for (String colum : columnsSourceSet) {
                Matcher matcher1 = _COLUMN_NAME_PATTERN.matcher(colum);

                while (matcher1.find()) {
                    if (matcher.group(1).equalsIgnoreCase(
                            matcher1.group(1))) {

                        columnsTarget = columnsTarget.replace(
                                matcher.group(), colum + ",");
                    }
                }
            }
        }

        return _getConstraints(columnsTarget, columnsSource);
    }

    private String _getColumns(
            String sourceColumns, String targetColumns) throws ReplacementException {
        Set<String> columnsTargetSet = _getColumnsSet(targetColumns, false);
        Set<String> columnsSourceSet = _getColumnsSet(sourceColumns, false);

        // Getting columns name from source data

        Set<String> onlyColumnsName = _getColumnsSet(sourceColumns, true);

        // Checking if exist custom columns

        columnsTargetSet.forEach(
                (column) -> {
                    Matcher matcher = _COLUMN_NAME_PATTERN.matcher(column);

                    if (matcher.find()) {
                        String columnTarget = matcher.group(1);

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
                    String trimmedColumn = column.trim();
                    fields.add(trimmedColumn);
                }
            }
        }

        return fields;
    }

    private String _getConstraints(
            String columns, String constraints) throws ReplacementException {

        try {
            String type = getType();

            if (type.equals(SupportedTypes.MYSQL)) {
                Pattern pattern = Pattern.compile(
                        "PRIMARY\\s+KEY\\s+(.+)(\\s*.*)+");
                Matcher matcher = pattern.matcher(constraints);

                StringBuilder sb = new StringBuilder();
                sb.append(columns);
                sb.append("\n");
                sb.append("  ");
                sb.append(matcher.group());

                return toString();
            }
            else if (type.equals(SupportedTypes.POSTGRES)) {
                return "WIP";
            }
            else {
                throw new ReplacementException(
                        "No supported database type %s".formatted(type));
            }
        }
        catch (ReplacementException replacementException) {
            throw new ReplacementException(replacementException);
        }

    }

    private List<Map<String, String>> _getContentResourceDirectory(
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
            throw new SQLFilesException(exception.getMessage());
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

    private String _removeLastComma(String content) {
        return content.replace(",", "");
    }

    // Patterns variables

    private static final Pattern _COLUMN_NAME_PATTERN = Pattern.compile(
            "(`[A-z]+_?`)\\s+[^,]+(?:,|$)");

    // Utilities variables

    private static final String _RESOURCE_DIRECTORY = "/src/main/resources/";

}

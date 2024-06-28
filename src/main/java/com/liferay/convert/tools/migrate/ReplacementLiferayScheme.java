package com.liferay.convert.tools.migrate;

import com.liferay.convert.tools.exception.ReplacementException;
import com.liferay.convert.tools.exception.SQLFilesException;
import com.liferay.convert.tools.util.PrintLoggerUtil;
import com.liferay.convert.tools.util.ResultsThreadLocal;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Albert Gomes Cabral
 */
public class ReplacementLiferayScheme extends BaseReplacement {

    @Override
    public void replacement(
            String sourceFileName, String targetFileName, String newFileName)
        throws Exception {

        try {
            List<Map<String, String>> contentMapList =
                    _getContentsFromFiles(sourceFileName, targetFileName);

            if (contentMapList != null && contentMapList.size() == 2) {
                String sourceContent = contentMapList.get(0).get("source.key");
                String targetContent = contentMapList.get(1).get("target.key");

                if (sourceContent != null && targetContent != null) {
                    Pattern[] patternsArray = new Pattern[] {
                            _CREATE_TABLE_GROUP_ID_FIELD_PATTERN,
                            _CREATE_TABLE_PATTERN,
                    };

                    for (Pattern pattern : patternsArray) {
                        targetContent = replaceContextPattern(
                                sourceContent, targetContent, pattern);
                    }

                    // Method to create output file and add on thread to be got in main class.

                    _createSQLFileOutput(newFileName, targetContent);
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
            throw new Exception(
                    "Unable to replace contents ", exception);
        }

    }

    protected String replaceContextPattern(
            String sourceContent, String targetContent, Pattern pattern)
        throws ReplacementException {

        try {
            Matcher matcherTarget = pattern.matcher(targetContent);

            while (matcherTarget.find()) {
                Matcher matcherSource = pattern.matcher(sourceContent);

                while (matcherSource.find()) {
                    if (matcherTarget.group(2).equalsIgnoreCase(matcherSource.group(2))) {
                        String tableNamePattern = pattern.toString();

                        // Replace special tables names concat with group id

                        if (tableNamePattern.contains(
                                "(([A-z]+)(_[A-z]+_)([0-9]+))")) {

                            String name = matcherSource.group(2);
                            String groupId = matcherTarget.group(4);

                            String nameConcatGroupId = name + matcherSource.group(3) + groupId;

                            // Replace name concat group id

                            targetContent = _replacementContent(
                                    Objects.requireNonNull(targetContent),
                                    matcherTarget.group(1), nameConcatGroupId, pattern);

                            // Replace table's definitions

                            targetContent = _replacementContent(
                                    Objects.requireNonNull(targetContent),
                                    matcherTarget.group(5), matcherSource.group(5), pattern);
                        }
                        else {
                            // Replace all table's name

                            targetContent = _replacementContent(
                                    Objects.requireNonNull(targetContent),
                                    matcherTarget.group(1), matcherSource.group(1), pattern);

                            // Replace table definitions

                            String definitionsSource = matcherSource.group(2);
                            String definitionsTarget = matcherTarget.group(2);

                            // Getting definitions

                            String definitions = _getColumns(
                                    definitionsSource, definitionsTarget);

                            targetContent = _replacementContent(
                                    Objects.requireNonNull(targetContent),
                                    definitionsTarget, definitions, pattern);
                        }
                    }
                }
            }

            return targetContent;
        }
        catch (Exception exception) {
            throw new ReplacementException(
                    "Unable to process " + pattern.pattern(), exception);
        }

    }

    private Map<String, String> _buildMapItem(String key, String value) {
        Map<String, String> itemMap = new HashMap<>();

        itemMap.put(key, value);

        return itemMap;
    }

    private void _createSQLFileOutput(
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
            Set<String> columns, String originalColumns) {

        if (columns == null || columns.isEmpty()) {
            return null;
        }

        Pattern pattern = Pattern.compile(
                "(`\\w+`)\\s\\w+(\\(\\d+\\))?\\s.+,");

        Matcher matcher = pattern.matcher(originalColumns);

        while (matcher.find()) {
            for (String colum : columns) {
                Matcher matcher1 = _COLUMN_NAME_PATTERN.matcher(colum);

                if (matcher1.find()) {
                    if (matcher.group(1).equalsIgnoreCase(
                            matcher1.group(1))) {

                        originalColumns = originalColumns.replace(
                                matcher.group(), colum.trim() + ",");
                    }
                }
            }
        }

        return originalColumns;
    }

    private String _getColumns(
            String sourceColumns, String targetColumns) {

        Set<String> columnsTargetSet = _getColumnsSet(targetColumns, false);
        Set<String> sourceColumnsSet = _getColumnsSet(sourceColumns, false);
        Set<String> onlyColumnsNameSet = _getColumnsSet(sourceColumns, true);

        columnsTargetSet.forEach(
                (column) -> {
                    Matcher matcher1 = _COLUMN_NAME_PATTERN.matcher(column);

                    if (matcher1.find()) {
                        String columnTarget = matcher1.group(1);

                        if (!onlyColumnsNameSet.contains(columnTarget)) {
                            sourceColumnsSet.add(column);
                        };
                    }
                }
        );

        return _formatColumns(sourceColumnsSet, targetColumns);

    }

    private Set<String> _getColumnsSet(String fields, boolean onlyColumnName) {
        Set<String> fieldsSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        for (String column : fields.split(",")) {
            Matcher matcher = _COLUMN_NAME_PATTERN.matcher(column);

            if (matcher.find()) {
                if (onlyColumnName) {
                    fieldsSet.add(matcher.group(1));
                }
                else {
                    fieldsSet.add(column.trim());
                }
            }
        }

        return fieldsSet;
    }

    private List<Map<String, String>> _getContentsFromFiles(
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
                            "Cannot convert input stream to string!" +
                                    " Invalid input file.", null);

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

    private String _replacementContent(
            String content, String oldContent, String newContent, Pattern pattern) {
        String contentReplacement;

        contentReplacement = content.replace(oldContent, newContent);

        if (!contentReplacement.isBlank() || !contentReplacement.isEmpty()) {
            PrintLoggerUtil.printReplacement(
                    oldContent, newContent, pattern);

            return contentReplacement;
        }

        return null;
    }

    // Patterns variables

    private static final Pattern _COLUMN_NAME_PATTERN = Pattern.compile(
            "(`[A-z]+_?`)\\s+[^,]+(?:,|$)");

    private static final Pattern _CREATE_TABLE_PATTERN = Pattern.compile(
            "CREATE\\s+TABLE\\s+(`[A-z]+_?`)\\s*\\(((\\s*.*,)+(\\s*.*))\\s*\\)\\s*" +
                    "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci?;");

    private static final Pattern _CREATE_TABLE_GROUP_ID_FIELD_PATTERN = Pattern.compile(
            "CREATE\\s+TABLE\\s+(([A-z]+)(_[A-z]+_)([0-9]+))\\s*\\(((\\s*.*,)+(\\s*.*))\\s*\\)\\s*" +
                    "ENGINE=InnoDB\\s*DEFAULT\\s*CHARSET=utf8mb4\\s*COLLATE=utf8mb4_unicode_ci?;");


    // Utilities variables

    private static final String _RESOURCE_DIRECTORY = "/src/main/resources/";

}

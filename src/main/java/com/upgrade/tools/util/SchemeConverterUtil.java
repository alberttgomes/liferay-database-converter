package com.upgrade.tools.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Albert Gomes Cabral
 */
public class SchemeConverterUtil {

    public static String readContent(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            return stringBuilder.toString();
        }
    }

    public static List<String> readChunks(
        InputStream inputStream, int chunkSize, int capability) throws IOException {

        List<String> chunks = new ArrayList<>();

        StringBuilder chunkBuilder = new StringBuilder();

        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            int count = 0;
            int countChunks = 0;

            while ((line = bufferedReader.readLine()) != null) {
                chunkBuilder.append(line).append("\n");
                count++;

                if (count == chunkSize) {
                    countChunks++;

                    chunks.add(chunkBuilder.toString());
                    chunkBuilder.setLength(0);
                    count = 0;

                    if (capability <= countChunks) {
                        break;
                    }
                }
            }

            if (!chunkBuilder.isEmpty()) {
                chunks.add(chunkBuilder.toString());
            }
        }

        return chunks;
    }

}

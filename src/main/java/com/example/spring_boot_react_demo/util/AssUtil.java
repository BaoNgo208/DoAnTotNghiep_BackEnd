package com.example.spring_boot_react_demo.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import static com.example.spring_boot_react_demo.util.Constants.*;

public class AssUtil {
    public static File createAssFile(String text) {
        File file = new File(LYRICS_FILE);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    public static String readHeaderFromFile() throws IOException {
        return Files.readString(Paths.get(HEADER_FILE_PATH), StandardCharsets.UTF_8);
    }

    public static String modifyASSContent(String inputText) {
        StringBuilder modifiedText = new StringBuilder();
        String[] lines = inputText.split(NEW_LINE);

        for (String line : lines) {
            if (line.startsWith("Dialogue:") && !line.matches(".*\\{\\\\k[\\d]+\\}.*")) {
                modifiedText.append(modifyDialogue(line)).append(NEW_LINE);
            } else {
                modifiedText.append(line).append(NEW_LINE);
            }
        }

        return modifiedText.toString();
    }

    private static String modifyDialogue(String line) {
        String[] parts = line.split(COMMA, DIALOGUE_PARTS);
        if (parts.length < DIALOGUE_PARTS) return line;

        String text = parts[TEXT_INDEX];
        String[] words = text.split(SPACE);
        StringBuilder modifiedText = new StringBuilder();

        int timings = calculateKaraokeTimings(parts);
        for (int i = 0; i < words.length; i++) {
            if (!words[i].isEmpty()) {
                modifiedText.append("{\\k" + timings + "}").append(words[i]).append(SPACE);
            }
        }

        parts[TEXT_INDEX] = modifiedText.toString().trim();
        return String.join(COMMA, parts);
    }

    public static int calculateKaraokeTimings(String[] parts) {
        double startTime = parseTime(parts[1]);
        double endTime = parseTime(parts[2]);
        int totalTime = (int) ((endTime - startTime) * 100);

        String text = parts[TEXT_INDEX];
        String[] words = text.split(SPACE);
        int wordCount = words.length;
        return totalTime / wordCount;
    }

    private static double parseTime(String time) {
        // Parses a timestamp string in the format "HH:MM:SS.mmm"
        String[] parts = time.split(":|\\.");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        int milliseconds = Integer.parseInt(parts[3]);

        return hours * 3600 + minutes * 60 + seconds + milliseconds / 100.0;
    }
}

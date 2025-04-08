package com.example.spring_boot_react_demo.util;

import com.example.spring_boot_react_demo.model.LyricSegment;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.example.spring_boot_react_demo.util.Constants.*;
import static com.example.spring_boot_react_demo.util.AssDialogueConstants.*;

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
            if (line.startsWith(DIALOGUE_TEXT) && !line.matches(".*\\{\\\\k[\\d]+\\}.*")) {
                modifiedText.append(modifyDialogueForKaraoke(line)).append(NEW_LINE);
            } else {
                modifiedText.append(line).append(NEW_LINE);
            }
        }

        return modifiedText.toString();
    }

    public static List<LyricSegment> convertAssTextToList(String text){
        List<LyricSegment> lyricSegments = new ArrayList<>();
        String[] lines = text.split(NEW_LINE);
        for (String line : lines) {
            if (line.startsWith(DIALOGUE_TEXT) && !line.matches(".*\\{\\\\k[\\d]+\\}.*")) {
                lyricSegments.add(convertLineToLyricSegment(line));
            }
        }
        return lyricSegments;
    }
    public static String convertListToAssText(List<LyricSegment> lyricSegments, String text){
        StringBuilder modifiedText = new StringBuilder();
        String[] lines = text.split(NEW_LINE);

        for (String line : lines) {
            if (!line.startsWith(DIALOGUE_TEXT)) {
                modifiedText.append(line).append(NEW_LINE);
            }
        }
        for (LyricSegment lyricSegment : lyricSegments) {
            modifiedText.append("Dialogue: 0,"
                    + formatTime(lyricSegment.getStartTime())
                    + COMMA
                    + formatTime(lyricSegment.getEndTime())
                    + ",Default,,0,0,0,,"
                    + lyricSegment.getText()).append(NEW_LINE);
        }
        return modifiedText.toString();
    }

    private static LyricSegment convertLineToLyricSegment(String line){

        String[] parts = line.split(COMMA, DIALOGUE_PARTS);
        if (parts.length >= DIALOGUE_PARTS) {
            double start = parseTime(parts[START_INDEX]);
            double end = parseTime(parts[END_INDEX]);
            String dialogueText = parts[TEXT_INDEX];
            if (!dialogueText.isEmpty()) {
                return new LyricSegment(dialogueText, start, end);
            }
        }
        return null;
    }

    private static String modifyDialogueForKaraoke(String line) {
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

    private static int calculateKaraokeTimings(String[] parts) {
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

        return hours * 3600 + minutes * 60 + seconds + milliseconds / 1000.0;
    }
    private static String formatTime(double timeInSeconds) {
        int totalMilliseconds = (int)(timeInSeconds * 1000);
        int hours = totalMilliseconds / 3600000;
        int minutes = (totalMilliseconds % 3600000) / 60000;
        int seconds = (totalMilliseconds % 60000) / 1000;
        int milliseconds = totalMilliseconds % 1000;

        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds);
    }
}

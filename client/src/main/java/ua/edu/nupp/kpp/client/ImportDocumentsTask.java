package ua.edu.nupp.kpp.client;

import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImportDocumentsTask extends Task<Integer> {

    private static final Logger LOG = Logger.getLogger(ImportDocumentsTask.class.getName());
    private static final String DELIMITER = ";";
    private static final String[] HEADER = {"author_id", "reg_number", "title", "doc_type",
            "due_date", "criticality", "dependent_processes"};

    private final Path file;
    private final HttpApprovalClient api;
    private volatile int total;
    private volatile int skipped;
    private volatile List<String> problems = List.of();

    public ImportDocumentsTask(Path file, HttpApprovalClient api) {
        this.file = file;
        this.api = api;
    }

    public int getTotal() {
        return total;
    }

    public int getSkipped() {
        return skipped;
    }

    public List<String> getProblems() {
        return problems;
    }

    @Override
    protected Integer call() {
        LOG.info("Імпорт CSV розпочато: " + file);
        updateMessage("Читання файла " + file.getFileName());
        updateProgress(0, 1);
        try {
            return importAll();
        } catch (IllegalStateException e) {
            LOG.warning("Імпорт CSV завершився помилкою: " + e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Непередбачена помилка імпорту CSV", e);
            throw new IllegalStateException("Непередбачена помилка імпорту CSV", e);
        }
    }

    private Integer importAll() {
        List<String> lines = readLines();
        checkHeader(lines.get(0));
        total = (int) lines.stream().skip(1).filter(line -> !line.isBlank()).count();
        if (total == 0) {
            updateMessage("Немає рядків для імпорту");
            updateProgress(1, 1);
            LOG.info("Імпорт CSV завершено: у файлі немає рядків з даними");
            return 0;
        }
        int imported = 0;
        int processed = 0;
        List<String> found = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            int lineNumber = i + 1;
            try {
                api.addDocument(parseLine(line));
                imported++;
            } catch (IllegalArgumentException | ServerRejectedException e) {
                LOG.warning("Рядок " + lineNumber + " пропущено: " + e.getMessage());
                found.add("Рядок " + lineNumber + ": " + e.getMessage());
            } catch (IllegalStateException e) {
                throw new IllegalStateException("Імпорт зупинено на рядку " + lineNumber
                        + " (імпортовано до зупинки: " + imported + "): " + e.getMessage(), e);
            }
            processed++;
            updateProgress(processed, total);
            updateMessage("Оброблено " + processed + " з " + total + ", імпортовано " + imported);
        }
        skipped = found.size();
        problems = List.copyOf(found);
        LOG.info("Імпорт CSV завершено: імпортовано " + imported + ", пропущено " + skipped + " з " + total);
        return imported;
    }

    private List<String> readLines() {
        if (!Files.isRegularFile(file)) {
            throw new IllegalStateException("CSV-файл не знайдено: " + file);
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (MalformedInputException e) {
            throw new IllegalStateException("CSV-файл має бути у кодуванні UTF-8: " + file, e);
        } catch (IOException e) {
            throw new IllegalStateException("Не вдалося прочитати CSV-файл: " + file, e);
        }
        if (lines.isEmpty() || lines.get(0).isBlank()) {
            throw new IllegalStateException("CSV-файл порожній: " + file);
        }
        return lines;
    }

    private static void checkHeader(String firstLine) {
        String text = firstLine.startsWith("﻿") ? firstLine.substring(1) : firstLine;
        String[] parts = text.split(DELIMITER, -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        if (!Arrays.equals(HEADER, parts)) {
            throw new IllegalStateException("Некоректний заголовок CSV. Очікується: "
                    + String.join("; ", HEADER));
        }
    }

    static Document parseLine(String line) {
        String[] p = line.split(DELIMITER, -1);
        if (p.length != HEADER.length) {
            throw new IllegalArgumentException("очікується " + HEADER.length + " полів, отримано " + p.length);
        }
        for (int i = 1; i <= 4; i++) {
            if (p[i].isBlank()) {
                throw new IllegalArgumentException("порожнє поле «" + HEADER[i] + "»");
            }
        }
        return new Document(null, longValue(p[0], HEADER[0]), p[1].trim(), p[2].trim(), p[3].trim(), p[4].trim(),
                intValue(p[5], HEADER[5]), intValue(p[6], HEADER[6]));
    }

    private static long longValue(String value, String field) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("поле «" + field + "» має бути цілим числом");
        }
    }

    private static int intValue(String value, String field) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("поле «" + field + "» має бути цілим числом");
        }
    }
}

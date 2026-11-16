package ua.edu.nupp.kpp.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public class DocumentCsvImporter {

    public record Result(int imported, int skipped) {}

    private static final Logger LOG = Logger.getLogger(DocumentCsvImporter.class.getName());
    private static final String DELIMITER = ";";
    private static final int COLUMNS = 7;

    private final HttpApprovalClient api;

    public DocumentCsvImporter(HttpApprovalClient api) {
        this.api = api;
    }

    public Result importFile(Path file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Не вдалося прочитати CSV-файл: " + file, e);
        }
        int imported = 0;
        int skipped = 0;
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            Document document;
            try {
                document = parseLine(line);
            } catch (IllegalArgumentException e) {
                LOG.warning("Пропущено рядок " + (i + 1) + ": " + e.getMessage());
                skipped++;
                continue;
            }
            api.addDocument(document);
            imported++;
        }
        LOG.info("CSV прочитано: імпортовано " + imported + ", пропущено " + skipped);
        return new Result(imported, skipped);
    }

    static Document parseLine(String line) {
        String[] p = line.split(DELIMITER, -1);
        if (p.length != COLUMNS) {
            throw new IllegalArgumentException("очікується " + COLUMNS + " полів, отримано " + p.length);
        }
        try {
            return new Document(null, Long.parseLong(p[0].trim()), p[1].trim(), p[2].trim(), p[3].trim(),
                    p[4].trim(), Integer.parseInt(p[5].trim()), Integer.parseInt(p[6].trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("некоректне числове значення");
        }
    }
}

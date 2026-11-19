package ua.edu.nupp.kpp.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

final class ClientLogging {

    private static final Logger ROOT = Logger.getLogger("ua.edu.nupp.kpp");

    private ClientLogging() {
    }

    static void init() {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %3$s: %5$s%n");
        try {
            Path dir = Path.of("logs");
            Files.createDirectories(dir);
            FileHandler handler = new FileHandler(dir.resolve("client.log").toString(), true);
            handler.setEncoding("UTF-8");
            handler.setFormatter(new SimpleFormatter());
            ROOT.addHandler(handler);
        } catch (IOException e) {
            ROOT.warning("Файл журналу недоступний: " + e.getMessage());
        }
    }
}

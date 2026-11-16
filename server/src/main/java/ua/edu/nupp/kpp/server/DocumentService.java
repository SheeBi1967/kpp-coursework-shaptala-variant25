package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocumentService {

    private static final Logger LOG = Logger.getLogger(DocumentService.class.getName());
    private final DocumentDAO dao = new DocumentDAO();
    private final AuthorDAO authorDAO = new AuthorDAO();

    public List<Document> getAll() throws SQLException {
        return dao.getAll();
    }

    public Document getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void add(Document d) throws SQLException {
        validate(d);
        requireAuthor(d.getAuthorId());
        requireUniqueRegNumber(d);
        try {
            dao.add(d);
        } catch (SQLException e) {
            throw failed("Помилка додавання документа", e);
        }
        LOG.info("Додано документ");
    }

    public void update(Document d) throws SQLException {
        validate(d);
        requireExisting(d.getId());
        requireAuthor(d.getAuthorId());
        requireUniqueRegNumber(d);
        try {
            dao.update(d);
        } catch (SQLException e) {
            throw failed("Помилка оновлення документа", e);
        }
        LOG.info("Оновлено документ");
    }

    public void delete(long id) throws SQLException {
        requireExisting(id);
        try {
            dao.delete(id);
        } catch (SQLException e) {
            throw failed("Помилка видалення документа", e);
        }
        LOG.info("Видалено документ");
    }

    static void validate(Document d) {
        if (d == null || d.getAuthorId() <= 0 || blank(d.getRegNumber()) || blank(d.getTitle())
                || blank(d.getDocType()) || blank(d.getDueDate())) {
            throw reject("Некоректні основні дані документа");
        }
        try {
            LocalDate.parse(d.getDueDate());
        } catch (DateTimeParseException e) {
            throw reject("Строк погодження має бути датою у форматі рррр-мм-дд");
        }
        if (d.getCriticality() < 1 || d.getCriticality() > 5) {
            throw reject("Критичність має бути в діапазоні 1..5");
        }
        if (d.getDependentProcesses() < 0) {
            throw reject("Кількість залежних процесів не може бути від'ємною");
        }
    }

    private void requireExisting(Long id) throws SQLException {
        if (id == null || dao.getById(id) == null) {
            throw reject("Документ не знайдено");
        }
    }

    private void requireAuthor(long id) throws SQLException {
        if (authorDAO.getById(id) == null) {
            throw reject("Автора не знайдено");
        }
    }

    private void requireUniqueRegNumber(Document d) throws SQLException {
        Document other = dao.getByRegNumber(d.getRegNumber());
        if (other != null && !other.getId().equals(d.getId())) {
            throw reject("Документ із таким реєстраційним номером уже існує");
        }
    }

    private static boolean blank(String v) {
        return v == null || v.isBlank();
    }

    private static IllegalArgumentException reject(String message) {
        LOG.warning(message);
        return new IllegalArgumentException(message);
    }

    private static SQLException failed(String message, SQLException e) {
        LOG.log(Level.SEVERE, message, e);
        return e;
    }
}

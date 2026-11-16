package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthorService {

    private static final Logger LOG = Logger.getLogger(AuthorService.class.getName());
    private final AuthorDAO dao = new AuthorDAO();

    public List<Author> getAll() throws SQLException {
        return dao.getAll();
    }

    public Author getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void add(Author a) throws SQLException {
        validate(a);
        try {
            dao.add(a);
        } catch (SQLException e) {
            throw failed("Помилка додавання автора", e);
        }
        LOG.info("Додано автора");
    }

    public void update(Author a) throws SQLException {
        validate(a);
        requireExisting(a.getId());
        try {
            dao.update(a);
        } catch (SQLException e) {
            throw failed("Помилка оновлення автора", e);
        }
        LOG.info("Оновлено автора");
    }

    public void delete(long id) throws SQLException {
        requireExisting(id);
        try {
            dao.delete(id);
        } catch (SQLException e) {
            throw failed("Помилка видалення автора", e);
        }
        LOG.info("Видалено автора");
    }

    static void validate(Author a) {
        if (a == null || blank(a.getFullName()) || blank(a.getDepartment()) || blank(a.getEmail())) {
            throw reject("Усі поля автора є обов'язковими");
        }
    }

    private void requireExisting(Long id) throws SQLException {
        if (id == null || dao.getById(id) == null) {
            throw reject("Автора не знайдено");
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

package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReviewerService {

    private static final Logger LOG = Logger.getLogger(ReviewerService.class.getName());
    private final ReviewerDAO dao = new ReviewerDAO();

    public List<Reviewer> getAll() throws SQLException {
        return dao.getAll();
    }

    public Reviewer getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void add(Reviewer v) throws SQLException {
        validate(v);
        try {
            dao.add(v);
        } catch (SQLException e) {
            throw failed("Помилка додавання рецензента", e);
        }
        LOG.info("Додано рецензента");
    }

    public void update(Reviewer v) throws SQLException {
        validate(v);
        requireExisting(v.getId());
        try {
            dao.update(v);
        } catch (SQLException e) {
            throw failed("Помилка оновлення рецензента", e);
        }
        LOG.info("Оновлено рецензента");
    }

    public void delete(long id) throws SQLException {
        requireExisting(id);
        try {
            dao.delete(id);
        } catch (SQLException e) {
            throw failed("Помилка видалення рецензента", e);
        }
        LOG.info("Видалено рецензента");
    }

    static void validate(Reviewer v) {
        if (v == null || blank(v.getFullName()) || blank(v.getJobTitle()) || blank(v.getEmail())) {
            throw reject("Усі поля рецензента є обов'язковими");
        }
    }

    private void requireExisting(Long id) throws SQLException {
        if (id == null || dao.getById(id) == null) {
            throw reject("Рецензента не знайдено");
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

package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApprovalRequestService {

    private static final Logger LOG = Logger.getLogger(ApprovalRequestService.class.getName());
    private final ApprovalRequestDAO dao = new ApprovalRequestDAO();
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final ReviewerDAO reviewerDAO = new ReviewerDAO();

    public List<ApprovalRequest> getAll() throws SQLException {
        return dao.getAll();
    }

    public ApprovalRequest getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void add(long documentId, long reviewerId) throws SQLException {
        ApprovalRequest request = fromDocument(null, documentId, reviewerId, ApprovalState.DRAFT);
        try {
            dao.add(request);
        } catch (SQLException e) {
            throw failed("Помилка створення запиту на погодження", e);
        }
        LOG.info("Створено запит на погодження");
    }

    public void update(long id, long documentId, long reviewerId) throws SQLException {
        ApprovalRequest old = dao.getById(id);
        if (old == null) {
            throw reject("Запит на погодження не знайдено");
        }
        ApprovalRequest request = fromDocument(id, documentId, reviewerId, old.getState());
        try {
            dao.update(request);
        } catch (SQLException e) {
            throw failed("Помилка оновлення запиту на погодження", e);
        }
        LOG.info("Оновлено запит на погодження");
    }

    public void changeState(long id, ApprovalState next) throws SQLException {
        ApprovalRequest current = dao.getById(id);
        if (current == null) {
            throw reject("Запит на погодження не знайдено");
        }
        if (next == null || !isAllowedTransition(current.getState(), next)) {
            LOG.warning("Недопустимий перехід стану: " + current.getState() + " -> " + next);
            throw new IllegalArgumentException("Недопустимий перехід стану");
        }
        try {
            dao.updateState(id, next);
        } catch (SQLException e) {
            throw failed("Помилка зміни стану запиту на погодження", e);
        }
        LOG.info("Змінено стан запиту на погодження: " + current.getState() + " -> " + next);
    }

    public void delete(long id) throws SQLException {
        if (dao.getById(id) == null) {
            throw reject("Запит на погодження не знайдено");
        }
        try {
            dao.delete(id);
        } catch (SQLException e) {
            throw failed("Помилка видалення запиту на погодження", e);
        }
        LOG.info("Видалено запит на погодження");
    }

    public static boolean isAllowedTransition(ApprovalState from, ApprovalState to) {
        return (from == ApprovalState.DRAFT && to == ApprovalState.SUBMITTED)
                || (from == ApprovalState.SUBMITTED && to == ApprovalState.REVIEW)
                || (from == ApprovalState.REVIEW && to == ApprovalState.APPROVED);
    }

    private ApprovalRequest fromDocument(Long id, long documentId, long reviewerId, ApprovalState state) throws SQLException {
        Document d = documentDAO.getById(documentId);
        if (d == null) {
            throw reject("Документ не знайдено");
        }
        if (reviewerDAO.getById(reviewerId) == null) {
            throw reject("Рецензента не знайдено");
        }
        double f1 = PriorityCalculator.factor1(LocalDate.parse(d.getDueDate()), LocalDate.now());
        double f2 = PriorityCalculator.factor2(d.getCriticality());
        double f3 = PriorityCalculator.factor3(d.getDependentProcesses());
        return new ApprovalRequest(id, documentId, reviewerId, state, f1, f2, f3,
                PriorityCalculator.calculate(f1, f2, f3), null);
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

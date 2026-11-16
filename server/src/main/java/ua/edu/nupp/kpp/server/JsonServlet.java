package ua.edu.nupp.kpp.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class JsonServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(JsonServlet.class.getName());
    private final ObjectMapper mapper = new ObjectMapper();

    protected void writeJson(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        mapper.writeValue(resp.getWriter(), data);
    }

    protected void writeError(HttpServletResponse resp, Exception e) throws IOException {
        int status;
        String message;
        if (e instanceof IllegalArgumentException) {
            status = HttpServletResponse.SC_BAD_REQUEST;
            message = e.getMessage() == null ? "Некоректний запит" : e.getMessage();
        } else if (isIntegrityViolation(e)) {
            status = HttpServletResponse.SC_BAD_REQUEST;
            message = "Операцію неможливо виконати: запис пов'язаний з іншими записами";
        } else {
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            message = "Внутрішня помилка сервера";
            LOG.log(Level.SEVERE, "Непередбачена помилка під час обробки запиту", e);
        }
        resp.setStatus(status);
        writeJson(resp, Map.of("error", message));
    }

    protected static long longParam(HttpServletRequest req, String name) {
        String value = requiredParam(req, name);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметр " + name + " має бути цілим числом");
        }
    }

    protected static int intParam(HttpServletRequest req, String name) {
        String value = requiredParam(req, name);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметр " + name + " має бути цілим числом");
        }
    }

    protected static IllegalArgumentException unknownAction() {
        return new IllegalArgumentException("Невідома операція");
    }

    private static String requiredParam(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Параметр " + name + " є обов'язковим");
        }
        return value.trim();
    }

    private static boolean isIntegrityViolation(Exception e) {
        if (e instanceof SQLIntegrityConstraintViolationException) {
            return true;
        }
        if (e instanceof SQLException sql) {
            String state = sql.getSQLState();
            return state != null && state.startsWith("23");
        }
        return false;
    }
}

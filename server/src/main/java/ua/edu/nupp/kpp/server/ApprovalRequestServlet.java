package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ApprovalRequestServlet extends JsonServlet {

    private final ApprovalRequestService service = new ApprovalRequestService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            writeJson(resp, service.getAll());
        } catch (Exception e) {
            writeError(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        try {
            String action = req.getParameter("action");
            if ("add".equals(action)) {
                service.add(longParam(req, "documentId"), longParam(req, "reviewerId"));
            } else if ("update".equals(action)) {
                service.update(longParam(req, "id"), longParam(req, "documentId"), longParam(req, "reviewerId"));
            } else if ("transition".equals(action)) {
                service.changeState(longParam(req, "id"), state(req.getParameter("state")));
            } else if ("delete".equals(action)) {
                service.delete(longParam(req, "id"));
            } else {
                throw unknownAction();
            }
            writeJson(resp, service.getAll());
        } catch (Exception e) {
            writeError(resp, e);
        }
    }

    private ApprovalState state(String value) {
        if (value != null) {
            for (ApprovalState s : ApprovalState.values()) {
                if (s.name().equals(value.trim())) {
                    return s;
                }
            }
        }
        throw new IllegalArgumentException("Невідомий стан запиту на погодження");
    }
}

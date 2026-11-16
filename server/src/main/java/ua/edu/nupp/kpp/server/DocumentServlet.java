package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DocumentServlet extends JsonServlet {

    private final DocumentService service = new DocumentService();

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
                service.add(read(req, null));
            } else if ("update".equals(action)) {
                service.update(read(req, longParam(req, "id")));
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

    private Document read(HttpServletRequest req, Long id) {
        return new Document(
                id,
                longParam(req, "authorId"),
                req.getParameter("regNumber"),
                req.getParameter("title"),
                req.getParameter("docType"),
                req.getParameter("dueDate"),
                intParam(req, "criticality"),
                intParam(req, "dependentProcesses"));
    }
}

package ua.edu.nupp.kpp.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class HttpApprovalClient {

    private static final String BASE = "http://localhost:8080/document-approval-server";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public List<Author> getAuthors() {
        return Arrays.asList(read(get("/authors"), Author[].class));
    }

    public void addAuthor(Author a) {
        post("/authors", form("action", "add", "fullName", a.getFullName(),
                "department", a.getDepartment(), "email", a.getEmail()));
    }

    public void updateAuthor(Author a) {
        post("/authors", form("action", "update", "id", String.valueOf(a.getId()), "fullName", a.getFullName(),
                "department", a.getDepartment(), "email", a.getEmail()));
    }

    public void deleteAuthor(long id) {
        post("/authors", form("action", "delete", "id", String.valueOf(id)));
    }

    public List<Reviewer> getReviewers() {
        return Arrays.asList(read(get("/reviewers"), Reviewer[].class));
    }

    public void addReviewer(Reviewer r) {
        post("/reviewers", form("action", "add", "fullName", r.getFullName(),
                "jobTitle", r.getJobTitle(), "email", r.getEmail()));
    }

    public void updateReviewer(Reviewer r) {
        post("/reviewers", form("action", "update", "id", String.valueOf(r.getId()), "fullName", r.getFullName(),
                "jobTitle", r.getJobTitle(), "email", r.getEmail()));
    }

    public void deleteReviewer(long id) {
        post("/reviewers", form("action", "delete", "id", String.valueOf(id)));
    }

    public List<Document> getDocuments() {
        return Arrays.asList(read(get("/documents"), Document[].class));
    }

    public void addDocument(Document d) {
        post("/documents", documentForm("add", d));
    }

    public void updateDocument(Document d) {
        post("/documents", documentForm("update", d) + "&id=" + d.getId());
    }

    public void deleteDocument(long id) {
        post("/documents", form("action", "delete", "id", String.valueOf(id)));
    }

    public List<ApprovalRequest> getRequests() {
        return Arrays.asList(read(get("/requests"), ApprovalRequest[].class));
    }

    public void addRequest(long documentId, long reviewerId) {
        post("/requests", form("action", "add", "documentId", String.valueOf(documentId),
                "reviewerId", String.valueOf(reviewerId)));
    }

    public void updateRequest(long id, long documentId, long reviewerId) {
        post("/requests", form("action", "update", "id", String.valueOf(id),
                "documentId", String.valueOf(documentId), "reviewerId", String.valueOf(reviewerId)));
    }

    public void changeRequestState(long id, ApprovalState state) {
        post("/requests", form("action", "transition", "id", String.valueOf(id),
                "state", state == null ? "" : state.name()));
    }

    public void deleteRequest(long id) {
        post("/requests", form("action", "delete", "id", String.valueOf(id)));
    }

    private String documentForm(String action, Document d) {
        return form("action", action, "authorId", String.valueOf(d.getAuthorId()),
                "regNumber", d.getRegNumber(), "title", d.getTitle(), "docType", d.getDocType(),
                "dueDate", d.getDueDate(), "criticality", String.valueOf(d.getCriticality()),
                "dependentProcesses", String.valueOf(d.getDependentProcesses()));
    }

    private static String form(String... pairs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pairs.length; i += 2) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(pairs[i]).append('=').append(enc(pairs[i + 1]));
        }
        return sb.toString();
    }

    private String get(String path) {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + path))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        return send(req);
    }

    private String post(String path, String form) {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + path))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                .build();
        return send(req);
    }

    private String send(HttpRequest req) {
        HttpResponse<String> response;
        try {
            response = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Запит до сервера перервано", e);
        } catch (IOException e) {
            throw new IllegalStateException("Сервер недоступний. Перевірте, чи запущено Tomcat: " + BASE, e);
        }
        if (response.statusCode() >= 400) {
            throw new IllegalStateException(errorText(response));
        }
        return response.body();
    }

    private String errorText(HttpResponse<String> response) {
        try {
            JsonNode node = mapper.readTree(response.body());
            if (node != null && node.hasNonNull("error")) {
                return node.get("error").asText();
            }
        } catch (IOException e) {
            return "Помилка HTTP " + response.statusCode();
        }
        return "Помилка HTTP " + response.statusCode();
    }

    private <T> T read(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            throw new IllegalStateException("Помилка JSON", e);
        }
    }

    private static String enc(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}

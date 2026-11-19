package ua.edu.nupp.kpp.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public class MainApp extends Application {

    private static final Logger LOG = Logger.getLogger(MainApp.class.getName());
    private static final Path CSV_FILE = Path.of("sample-data", "documents.csv");

    private final HttpApprovalClient api = new HttpApprovalClient();
    private final TableView<Author> authors = new TableView<>();
    private final TableView<Reviewer> reviewers = new TableView<>();
    private final TableView<Document> documents = new TableView<>();
    private final TableView<ApprovalRequest> requests = new TableView<>();
    private final Button importCsv = new Button("Імпорт CSV");
    private final ProgressBar importProgress = new ProgressBar(0);
    private final Label importStatus = new Label("Імпорт не виконувався");
    private ImportDocumentsTask importTask;

    @Override
    public void start(Stage stage) {
        TabPane tabs = new TabPane(
                new Tab("Автори", authorPane()),
                new Tab("Рецензенти", reviewerPane()),
                new Tab("Документи", documentPane()),
                new Tab("Запити на погодження", requestPane()));
        tabs.getTabs().forEach(t -> t.setClosable(false));
        stage.setScene(new Scene(tabs, 1180, 680));
        stage.setTitle("Інформаційна система погодження внутрішніх документів");
        stage.show();
        Platform.runLater(this::refreshAll);
    }

    private Pane authorPane() {
        authors.getColumns().setAll(col("ID", "id", 60), col("ПІБ", "fullName", 240),
                col("Підрозділ", "department", 220), col("Email", "email", 220));
        TextField fullName = new TextField();
        TextField department = new TextField();
        TextField email = new TextField();
        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");
        add.setOnAction(e -> safe(() -> {
            api.addAuthor(new Author(null, fullName.getText(), department.getText(), email.getText()));
            loadAuthors();
        }));
        update.setOnAction(e -> safe(() -> {
            Author a = selected(authors);
            api.updateAuthor(new Author(a.getId(), fullName.getText(), department.getText(), email.getText()));
            loadAuthors();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteAuthor(selected(authors).getId());
            loadAll();
        }));
        refresh.setOnAction(e -> safe(this::loadAuthors));
        authors.getSelectionModel().selectedItemProperty().addListener((o, old, a) -> {
            if (a != null) {
                fullName.setText(a.getFullName());
                department.setText(a.getDepartment());
                email.setText(a.getEmail());
            }
        });
        return layout(authors, new VBox(8,
                new Label("ПІБ"), fullName,
                new Label("Підрозділ"), department,
                new Label("Email"), email,
                new FlowPane(8, 8, add, update, delete, refresh)));
    }

    private Pane reviewerPane() {
        reviewers.getColumns().setAll(col("ID", "id", 60), col("ПІБ", "fullName", 240),
                col("Посада", "jobTitle", 220), col("Email", "email", 220));
        TextField fullName = new TextField();
        TextField jobTitle = new TextField();
        TextField email = new TextField();
        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");
        add.setOnAction(e -> safe(() -> {
            api.addReviewer(new Reviewer(null, fullName.getText(), jobTitle.getText(), email.getText()));
            loadReviewers();
        }));
        update.setOnAction(e -> safe(() -> {
            Reviewer r = selected(reviewers);
            api.updateReviewer(new Reviewer(r.getId(), fullName.getText(), jobTitle.getText(), email.getText()));
            loadReviewers();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteReviewer(selected(reviewers).getId());
            loadAll();
        }));
        refresh.setOnAction(e -> safe(this::loadReviewers));
        reviewers.getSelectionModel().selectedItemProperty().addListener((o, old, r) -> {
            if (r != null) {
                fullName.setText(r.getFullName());
                jobTitle.setText(r.getJobTitle());
                email.setText(r.getEmail());
            }
        });
        return layout(reviewers, new VBox(8,
                new Label("ПІБ"), fullName,
                new Label("Посада"), jobTitle,
                new Label("Email"), email,
                new FlowPane(8, 8, add, update, delete, refresh)));
    }

    private Pane documentPane() {
        documents.getColumns().setAll(col("ID", "id", 50), col("ID автора", "authorId", 80),
                col("Реєстр. номер", "regNumber", 110), col("Назва", "title", 260), col("Тип", "docType", 110),
                col("Строк", "dueDate", 90), col("Критичність", "criticality", 90),
                col("Залежні процеси", "dependentProcesses", 110));
        TextField authorId = new TextField();
        TextField regNumber = new TextField();
        TextField title = new TextField();
        TextField docType = new TextField();
        TextField dueDate = new TextField();
        TextField criticality = new TextField();
        TextField dependentProcesses = new TextField();
        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");
        importProgress.setMaxWidth(Double.MAX_VALUE);
        importStatus.setWrapText(true);
        add.setOnAction(e -> safe(() -> {
            api.addDocument(readDocument(null, authorId, regNumber, title, docType, dueDate,
                    criticality, dependentProcesses));
            loadDocuments();
        }));
        update.setOnAction(e -> safe(() -> {
            Document d = selected(documents);
            api.updateDocument(readDocument(d.getId(), authorId, regNumber, title, docType, dueDate,
                    criticality, dependentProcesses));
            loadDocuments();
            loadRequests();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteDocument(selected(documents).getId());
            loadAll();
        }));
        refresh.setOnAction(e -> safe(this::loadDocuments));
        importCsv.setOnAction(e -> startImport());
        documents.getSelectionModel().selectedItemProperty().addListener((o, old, d) -> {
            if (d != null) {
                authorId.setText(String.valueOf(d.getAuthorId()));
                regNumber.setText(d.getRegNumber());
                title.setText(d.getTitle());
                docType.setText(d.getDocType());
                dueDate.setText(d.getDueDate());
                criticality.setText(String.valueOf(d.getCriticality()));
                dependentProcesses.setText(String.valueOf(d.getDependentProcesses()));
            }
        });
        return layout(documents, new VBox(6,
                new Label("ID автора"), authorId,
                new Label("Реєстраційний номер"), regNumber,
                new Label("Назва"), title,
                new Label("Тип документа"), docType,
                new Label("Строк погодження (рррр-мм-дд)"), dueDate,
                new Label("Критичність (1..5)"), criticality,
                new Label("Кількість залежних процесів"), dependentProcesses,
                new FlowPane(8, 8, add, update, delete, refresh),
                importCsv, importProgress, importStatus));
    }

    private void startImport() {
        if (importTask != null && importTask.isRunning()) {
            return;
        }
        ImportDocumentsTask task = new ImportDocumentsTask(CSV_FILE, api);
        importTask = task;
        importCsv.setDisable(true);
        importProgress.progressProperty().bind(task.progressProperty());
        importStatus.textProperty().bind(task.messageProperty());
        task.setOnSucceeded(e -> importSucceeded(task));
        task.setOnFailed(e -> importFailed(task));
        Thread thread = new Thread(task, "csv-import");
        thread.setDaemon(true);
        thread.start();
    }

    private void importSucceeded(ImportDocumentsTask task) {
        importProgress.progressProperty().unbind();
        importStatus.textProperty().unbind();
        importCsv.setDisable(false);
        importProgress.setProgress(1);
        if (task.getTotal() == 0) {
            importStatus.setText("Немає рядків для імпорту");
        } else {
            importStatus.setText("Імпортовано: " + task.getValue() + ", пропущено: " + task.getSkipped());
        }
        safe(this::loadDocuments);
        if (task.getSkipped() > 0) {
            showSkipped(task);
        }
    }

    private void importFailed(ImportDocumentsTask task) {
        importProgress.progressProperty().unbind();
        importStatus.textProperty().unbind();
        importCsv.setDisable(false);
        importProgress.setProgress(0);
        importStatus.setText("Імпорт завершився помилкою");
        showError(task.getException());
        try {
            loadDocuments();
        } catch (RuntimeException e) {
            LOG.warning(e.getMessage());
        }
    }

    private void showSkipped(ImportDocumentsTask task) {
        List<String> problems = task.getProblems();
        StringBuilder text = new StringBuilder();
        problems.stream().limit(10).forEach(p -> text.append(p).append('\n'));
        if (problems.size() > 10) {
            text.append("… та ще ").append(problems.size() - 10);
        }
        Alert alert = new Alert(Alert.AlertType.WARNING, text.toString().trim(), ButtonType.OK);
        alert.setTitle("Імпорт завершено з пропусками");
        alert.setHeaderText("Імпортовано: " + task.getValue() + ", пропущено: " + task.getSkipped());
        alert.showAndWait();
    }

    private Pane requestPane() {
        requests.getColumns().setAll(col("ID", "id", 50), col("ID документа", "documentId", 100),
                col("ID рецензента", "reviewerId", 100), col("Стан", "state", 100), col("F1", "f1", 70),
                col("F2", "f2", 70), col("F3", "f3", 70), col("I", "priorityIndex", 80),
                col("Створено", "createdAt", 150));
        TextField documentId = new TextField();
        TextField reviewerId = new TextField();
        ComboBox<ApprovalState> state = new ComboBox<>(FXCollections.observableArrayList(ApprovalState.values()));
        Button add = new Button("Створити");
        Button update = new Button("Перерахувати");
        Button change = new Button("Змінити стан");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");
        add.setOnAction(e -> safe(() -> {
            api.addRequest(number(documentId, "ID документа"), number(reviewerId, "ID рецензента"));
            loadRequests();
        }));
        update.setOnAction(e -> safe(() -> {
            api.updateRequest(selected(requests).getId(), number(documentId, "ID документа"),
                    number(reviewerId, "ID рецензента"));
            loadRequests();
        }));
        change.setOnAction(e -> safe(() -> {
            ApprovalRequest r = selected(requests);
            if (state.getValue() == null) {
                throw new IllegalArgumentException("Виберіть новий стан");
            }
            api.changeRequestState(r.getId(), state.getValue());
            loadRequests();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteRequest(selected(requests).getId());
            loadRequests();
        }));
        refresh.setOnAction(e -> safe(this::loadRequests));
        requests.getSelectionModel().selectedItemProperty().addListener((o, old, r) -> {
            if (r != null) {
                documentId.setText(String.valueOf(r.getDocumentId()));
                reviewerId.setText(String.valueOf(r.getReviewerId()));
            }
        });
        return layout(requests, new VBox(8,
                new Label("ID документа"), documentId,
                new Label("ID рецензента"), reviewerId,
                new Label("Новий стан"), state,
                new FlowPane(8, 8, add, update),
                new FlowPane(8, 8, change, delete, refresh)));
    }

    private Document readDocument(Long id, TextField authorId, TextField regNumber, TextField title,
                                  TextField docType, TextField dueDate, TextField criticality,
                                  TextField dependentProcesses) {
        return new Document(id, number(authorId, "ID автора"), regNumber.getText(), title.getText(),
                docType.getText(), dueDate.getText(), integer(criticality, "Критичність"),
                integer(dependentProcesses, "Кількість залежних процесів"));
    }

    private long number(TextField field, String name) {
        try {
            return Long.parseLong(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + name + "» має бути цілим числом");
        }
    }

    private int integer(TextField field, String name) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + name + "» має бути цілим числом");
        }
    }

    private void refreshAll() {
        safe(this::loadAll);
    }

    private void loadAll() {
        loadAuthors();
        loadReviewers();
        loadDocuments();
        loadRequests();
    }

    private void loadAuthors() {
        authors.setItems(FXCollections.observableArrayList(api.getAuthors()));
    }

    private void loadReviewers() {
        reviewers.setItems(FXCollections.observableArrayList(api.getReviewers()));
    }

    private void loadDocuments() {
        documents.setItems(FXCollections.observableArrayList(api.getDocuments()));
    }

    private void loadRequests() {
        requests.setItems(FXCollections.observableArrayList(api.getRequests()));
    }

    private <T> T selected(TableView<T> table) {
        T value = table.getSelectionModel().getSelectedItem();
        if (value == null) {
            throw new IllegalArgumentException("Виберіть запис");
        }
        return value;
    }

    private void safe(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            LOG.warning(e.getMessage());
            showError(e);
        }
    }

    private void showError(Throwable e) {
        String message = e.getMessage() == null ? "Помилка" : e.getMessage();
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Помилка");
        alert.setHeaderText(null);
        alert.getDialogPane().setMinWidth(560);
        alert.showAndWait();
    }

    private Pane layout(TableView<?> table, Pane form) {
        form.setPadding(new Insets(10));
        form.setMinWidth(330);
        SplitPane split = new SplitPane(table, form);
        split.setDividerPositions(0.66);
        BorderPane root = new BorderPane(split);
        root.setPadding(new Insets(10));
        return root;
    }

    private <S, T> TableColumn<S, T> col(String title, String property, double width) {
        TableColumn<S, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        return column;
    }

    public static void main(String[] args) {
        ClientLogging.init();
        launch(args);
    }
}

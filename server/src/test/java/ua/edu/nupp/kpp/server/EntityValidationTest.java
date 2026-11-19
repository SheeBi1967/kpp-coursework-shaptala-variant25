package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntityValidationTest {

    private static Document document(String title, String dueDate, int criticality, int processes) {
        return new Document(null, 1L, "DOC-2026-100", title, "Наказ", dueDate, criticality, processes);
    }

    @Test
    void correctAuthorPassesValidation() {
        Author author = new Author(null, "Коваленко Олена Петрівна", "Навчальний відділ", "o.kovalenko@example.com");

        assertDoesNotThrow(() -> AuthorService.validate(author));
    }

    @Test
    void authorWithBlankFullNameIsRejected() {
        Author author = new Author(null, "   ", "Навчальний відділ", "o.kovalenko@example.com");

        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> AuthorService.validate(author));

        assertEquals("Усі поля автора є обов'язковими", error.getMessage());
    }

    @Test
    void reviewerWithoutEmailIsRejected() {
        Reviewer reviewer = new Reviewer(null, "Шевченко Віктор Іванович", "Проректор", null);

        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> ReviewerService.validate(reviewer));

        assertEquals("Усі поля рецензента є обов'язковими", error.getMessage());
    }

    @Test
    void correctDocumentPassesValidation() {
        assertDoesNotThrow(() -> DocumentService.validate(document("Наказ про відпустки", "2026-12-01", 3, 2)));
    }

    @Test
    void documentWithoutTitleIsRejected() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> DocumentService.validate(document("", "2026-12-01", 3, 2)));

        assertEquals("Некоректні основні дані документа", error.getMessage());
    }

    @Test
    void documentWithWrongDateFormatIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> DocumentService.validate(document("Наказ", "01.12.2026", 3, 2)));
        assertThrows(IllegalArgumentException.class,
                () -> DocumentService.validate(document("Наказ", "2026-02-30", 3, 2)));
    }

    @Test
    void documentWithCriticalityOutOfRangeIsRejected() {
        IllegalArgumentException tooHigh = assertThrows(IllegalArgumentException.class,
                () -> DocumentService.validate(document("Наказ", "2026-12-01", 6, 2)));
        assertThrows(IllegalArgumentException.class,
                () -> DocumentService.validate(document("Наказ", "2026-12-01", 0, 2)));

        assertEquals("Критичність має бути в діапазоні 1..5", tooHigh.getMessage());
    }

    @Test
    void documentWithNegativeDependentProcessesIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> DocumentService.validate(document("Наказ", "2026-12-01", 3, -1)));
    }
}

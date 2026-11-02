-- Курсова робота з дисципліни «Кросплатформене програмування»
-- Варіант 25: Інформаційна система погодження внутрішніх документів
-- Скрипт створює базу даних з нуля: видаляє попередню навчальну базу,
-- створює таблиці, первинні та зовнішні ключі й додає демонстраційні дані.

DROP DATABASE IF EXISTS document_approval;
CREATE DATABASE document_approval CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE document_approval;

-- Автор документа
CREATE TABLE author (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name  VARCHAR(160) NOT NULL,
    department VARCHAR(160) NOT NULL,
    email      VARCHAR(160) NOT NULL
) ENGINE = InnoDB;

-- Рецензент (особа, яка розглядає запит на погодження)
CREATE TABLE reviewer (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(160) NOT NULL,
    job_title VARCHAR(160) NOT NULL,
    email     VARCHAR(160) NOT NULL
) ENGINE = InnoDB;

-- Внутрішній документ.
-- due_date — строк погодження (для F1), criticality — рівень критичності 1..5 (для F2),
-- dependent_processes — кількість залежних процесів (для F3).
CREATE TABLE document (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id           BIGINT       NOT NULL,
    reg_number          VARCHAR(40)  NOT NULL UNIQUE,
    title               VARCHAR(255) NOT NULL,
    doc_type            VARCHAR(60)  NOT NULL,
    due_date            DATE         NOT NULL,
    criticality         INT          NOT NULL,
    dependent_processes INT          NOT NULL,
    CONSTRAINT fk_document_author FOREIGN KEY (author_id) REFERENCES author (id),
    CONSTRAINT chk_document_criticality CHECK (criticality BETWEEN 1 AND 5),
    CONSTRAINT chk_document_dependent CHECK (dependent_processes >= 0)
) ENGINE = InnoDB;

-- Запит на погодження — об'єкт життєвого циклу:
-- DRAFT (ЧЕРНЕТКА) -> SUBMITTED (ПОДАНО) -> REVIEW (РОЗГЛЯД) -> APPROVED (СХВАЛЕНО).
-- f1, f2, f3 — нормовані фактори 0..100, priority_index — інтегральний показник
-- I = 0,40*F1 + 0,35*F2 + 0,25*F3.
CREATE TABLE approval_request (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id    BIGINT      NOT NULL,
    reviewer_id    BIGINT      NOT NULL,
    state          VARCHAR(20) NOT NULL,
    f1             DOUBLE      NOT NULL,
    f2             DOUBLE      NOT NULL,
    f3             DOUBLE      NOT NULL,
    priority_index DOUBLE      NOT NULL,
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_request_document FOREIGN KEY (document_id) REFERENCES document (id),
    CONSTRAINT fk_request_reviewer FOREIGN KEY (reviewer_id) REFERENCES reviewer (id),
    CONSTRAINT chk_request_state CHECK (state IN ('DRAFT', 'SUBMITTED', 'REVIEW', 'APPROVED')),
    CONSTRAINT chk_request_f1 CHECK (f1 BETWEEN 0 AND 100),
    CONSTRAINT chk_request_f2 CHECK (f2 BETWEEN 0 AND 100),
    CONSTRAINT chk_request_f3 CHECK (f3 BETWEEN 0 AND 100),
    CONSTRAINT chk_request_index CHECK (priority_index BETWEEN 0 AND 100)
) ENGINE = InnoDB;

-- Початкові демонстраційні дані

INSERT INTO author (full_name, department, email) VALUES
    ('Коваленко Олена Петрівна', 'Навчальний відділ', 'o.kovalenko@example.com'),
    ('Мельник Андрій Сергійович', 'Відділ кадрів', 'a.melnyk@example.com'),
    ('Бондаренко Ірина Василівна', 'Бухгалтерія', 'i.bondarenko@example.com');

INSERT INTO reviewer (full_name, job_title, email) VALUES
    ('Шевченко Віктор Іванович', 'Проректор з навчальної роботи', 'v.shevchenko@example.com'),
    ('Ткаченко Наталія Олексіївна', 'Начальник юридичного відділу', 'n.tkachenko@example.com'),
    ('Литвиненко Богдан Михайлович', 'Головний бухгалтер', 'b.lytvynenko@example.com');

INSERT INTO document
    (author_id, reg_number, title, doc_type, due_date, criticality, dependent_processes)
VALUES
    (1, 'DOC-2026-001', 'Положення про організацію освітнього процесу', 'Положення', '2026-11-20', 5, 8),
    (2, 'DOC-2026-002', 'Наказ про відпустки співробітників на 2027 рік', 'Наказ', '2026-12-10', 3, 3),
    (3, 'DOC-2026-003', 'Кошторис витрат на закупівлю обладнання', 'Кошторис', '2026-11-06', 4, 5),
    (1, 'DOC-2026-004', 'Службова записка щодо зміни розкладу занять', 'Службова записка', '2026-11-30', 2, 1);

-- Значення f1, f2, f3 та priority_index наведено станом на 02.11.2026.
INSERT INTO approval_request
    (document_id, reviewer_id, state, f1, f2, f3, priority_index, created_at)
VALUES
    (1, 1, 'REVIEW',    40.00, 100.0, 80.0, 71.00, '2026-10-28 09:15:00'),
    (3, 3, 'SUBMITTED', 86.67,  75.0, 50.0, 73.42, '2026-10-30 14:40:00'),
    (2, 2, 'DRAFT',      0.00,  50.0, 30.0, 25.00, '2026-11-02 08:30:00'),
    (4, 1, 'APPROVED',   6.67,  25.0, 10.0, 13.92, '2026-10-20 11:00:00'),
    (1, 2, 'DRAFT',     40.00, 100.0, 80.0, 71.00, '2026-11-02 08:45:00');

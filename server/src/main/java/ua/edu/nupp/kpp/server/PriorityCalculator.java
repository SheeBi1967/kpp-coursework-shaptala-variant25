package ua.edu.nupp.kpp.server;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PriorityCalculator {

    private static final long DEADLINE_WINDOW_DAYS = 30;
    private static final int MIN_CRITICALITY = 1;
    private static final int MAX_CRITICALITY = 5;
    private static final int MAX_PROCESSES = 10;

    public static double factor1(LocalDate dueDate, LocalDate today) {
        if (dueDate == null || today == null) {
            throw new IllegalArgumentException("Некоректні дані для F1");
        }
        return factor1(ChronoUnit.DAYS.between(today, dueDate));
    }

    public static double factor1(long daysLeft) {
        if (daysLeft <= 0) {
            return 100.0;
        }
        if (daysLeft >= DEADLINE_WINDOW_DAYS) {
            return 0.0;
        }
        return round((DEADLINE_WINDOW_DAYS - daysLeft) / (double) DEADLINE_WINDOW_DAYS * 100.0);
    }

    public static double factor2(int criticality) {
        if (criticality < MIN_CRITICALITY || criticality > MAX_CRITICALITY) {
            throw new IllegalArgumentException("Критичність має бути в діапазоні 1..5");
        }
        return round((criticality - MIN_CRITICALITY) / (double) (MAX_CRITICALITY - MIN_CRITICALITY) * 100.0);
    }

    public static double factor3(int dependentProcesses) {
        if (dependentProcesses < 0) {
            throw new IllegalArgumentException("Кількість залежних процесів не може бути від'ємною");
        }
        return round(Math.min(dependentProcesses, MAX_PROCESSES) / (double) MAX_PROCESSES * 100.0);
    }

    public static double calculate(double f1, double f2, double f3) {
        check(f1);
        check(f2);
        check(f3);
        return round(0.40 * f1 + 0.35 * f2 + 0.25 * f3);
    }

    private static void check(double value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("Фактор має бути в діапазоні 0..100");
        }
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

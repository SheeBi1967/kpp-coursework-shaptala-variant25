package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PriorityCalculatorTest {

    private static final double DELTA = 0.001;

    @Test
    void factor1WithoutDeadlinePressureIsZero() {
        assertEquals(0.0, PriorityCalculator.factor1(30L), DELTA);
        assertEquals(0.0, PriorityCalculator.factor1(45L), DELTA);
    }

    @Test
    void factor1ForOverdueDocumentIsMaximum() {
        assertEquals(100.0, PriorityCalculator.factor1(0L), DELTA);
        assertEquals(100.0, PriorityCalculator.factor1(-7L), DELTA);
    }

    @Test
    void factor1DecreasesLinearlyInsideThirtyDayWindow() {
        assertEquals(50.0, PriorityCalculator.factor1(15L), DELTA);
        assertEquals(66.67, PriorityCalculator.factor1(10L), DELTA);
    }

    @Test
    void factor1IsCalculatedFromDatesUsingDaysBetweenTodayAndDueDate() {
        LocalDate today = LocalDate.of(2026, 11, 1);
        LocalDate dueDate = LocalDate.of(2026, 11, 16);

        double f1 = PriorityCalculator.factor1(dueDate, today);

        assertEquals(50.0, f1, DELTA);
    }

    @Test
    void factor2NormalizesCriticalityToRangeZeroToHundred() {
        assertEquals(0.0, PriorityCalculator.factor2(1), DELTA);
        assertEquals(50.0, PriorityCalculator.factor2(3), DELTA);
        assertEquals(100.0, PriorityCalculator.factor2(5), DELTA);
    }

    @Test
    void factor2RejectsCriticalityOutsideRangeOneToFive() {
        IllegalArgumentException tooLow =
                assertThrows(IllegalArgumentException.class, () -> PriorityCalculator.factor2(0));
        assertThrows(IllegalArgumentException.class, () -> PriorityCalculator.factor2(6));

        assertEquals("Критичність має бути в діапазоні 1..5", tooLow.getMessage());
    }

    @Test
    void factor3IsProportionalAndCappedAtTenProcesses() {
        assertEquals(0.0, PriorityCalculator.factor3(0), DELTA);
        assertEquals(40.0, PriorityCalculator.factor3(4), DELTA);
        assertEquals(100.0, PriorityCalculator.factor3(10), DELTA);
        assertEquals(100.0, PriorityCalculator.factor3(25), DELTA);
    }

    @Test
    void factor3RejectsNegativeNumberOfProcesses() {
        assertThrows(IllegalArgumentException.class, () -> PriorityCalculator.factor3(-1));
    }

    @Test
    void integralIndexUsesWeightsFortyThirtyFiveTwentyFive() {
        assertEquals(33.0, PriorityCalculator.calculate(20, 50, 30), DELTA);
        assertEquals(71.0, PriorityCalculator.calculate(40, 100, 80), DELTA);
    }

    @Test
    void integralIndexIsRoundedToTwoDecimals() {
        assertEquals(54.17, PriorityCalculator.calculate(66.67, 50, 40), DELTA);
    }

    @Test
    void integralIndexRejectsFactorOutsideRangeZeroToHundred() {
        assertThrows(IllegalArgumentException.class, () -> PriorityCalculator.calculate(101, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> PriorityCalculator.calculate(0, -1, 0));
    }

    @Test
    void priorityIndexOfDocumentMatchesManualCalculation() {
        LocalDate today = LocalDate.of(2026, 11, 1);
        LocalDate dueDate = LocalDate.of(2026, 11, 16);

        double f1 = PriorityCalculator.factor1(dueDate, today);
        double f2 = PriorityCalculator.factor2(5);
        double f3 = PriorityCalculator.factor3(4);

        assertEquals(65.0, PriorityCalculator.calculate(f1, f2, f3), DELTA);
    }
}

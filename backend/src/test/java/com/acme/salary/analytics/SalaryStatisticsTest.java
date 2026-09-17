package com.acme.salary.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the pure salary statistics helpers. These are the most
 * correctness-critical calculations (especially median), so they are covered
 * thoroughly with small, deterministic inputs.
 */
class SalaryStatisticsTest {

    @Test
    void average_ofSeveralValues_isRoundedToTwoDecimals() {
        List<BigDecimal> values = List.of(
                new BigDecimal("100"), new BigDecimal("200"), new BigDecimal("301"));
        // (100 + 200 + 301) / 3 = 200.333... -> 200.33
        assertThat(SalaryStatistics.average(values)).isEqualByComparingTo("200.33");
    }

    @Test
    void average_ofEmptyList_isZero() {
        assertThat(SalaryStatistics.average(List.of())).isEqualByComparingTo("0.00");
    }

    @Test
    void median_ofOddCount_isMiddleValue() {
        List<BigDecimal> values = List.of(
                new BigDecimal("30"), new BigDecimal("10"), new BigDecimal("20"));
        // sorted: 10, 20, 30 -> middle is 20
        assertThat(SalaryStatistics.median(values)).isEqualByComparingTo("20.00");
    }

    @Test
    void median_ofEvenCount_isAverageOfTwoMiddleValues() {
        List<BigDecimal> values = List.of(
                new BigDecimal("10"), new BigDecimal("20"),
                new BigDecimal("30"), new BigDecimal("40"));
        // sorted: 10,20,30,40 -> (20 + 30) / 2 = 25
        assertThat(SalaryStatistics.median(values)).isEqualByComparingTo("25.00");
    }

    @Test
    void median_ofSingleValue_isThatValue() {
        assertThat(SalaryStatistics.median(List.of(new BigDecimal("42"))))
                .isEqualByComparingTo("42.00");
    }

    @Test
    void median_isIndependentOfInputOrder() {
        List<BigDecimal> ascending = List.of(
                new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3"),
                new BigDecimal("4"), new BigDecimal("100"));
        List<BigDecimal> shuffled = List.of(
                new BigDecimal("100"), new BigDecimal("2"), new BigDecimal("4"),
                new BigDecimal("1"), new BigDecimal("3"));
        assertThat(SalaryStatistics.median(shuffled))
                .isEqualByComparingTo(SalaryStatistics.median(ascending))
                .isEqualByComparingTo("3.00");
    }

    @Test
    void median_ofEmptyList_isZero() {
        assertThat(SalaryStatistics.median(List.of())).isEqualByComparingTo("0.00");
    }

    @Test
    void sum_addsAllValues() {
        List<BigDecimal> values = List.of(
                new BigDecimal("1000.50"), new BigDecimal("2000.25"), new BigDecimal("999.25"));
        assertThat(SalaryStatistics.sum(values)).isEqualByComparingTo("4000.00");
    }

    @Test
    void min_and_max_findExtremes() {
        List<BigDecimal> values = List.of(
                new BigDecimal("500"), new BigDecimal("100"), new BigDecimal("900"));
        assertThat(SalaryStatistics.min(values)).isEqualByComparingTo("100.00");
        assertThat(SalaryStatistics.max(values)).isEqualByComparingTo("900.00");
    }

    @Test
    void min_and_max_ofEmptyList_areZero() {
        assertThat(SalaryStatistics.min(List.of())).isEqualByComparingTo("0.00");
        assertThat(SalaryStatistics.max(List.of())).isEqualByComparingTo("0.00");
    }
}

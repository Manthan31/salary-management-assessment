package com.acme.salary.analytics.dto;

import java.math.BigDecimal;

/**
 * Organization-wide compensation summary. All monetary values are in the base
 * currency (USD). Reflects active employees only unless otherwise filtered.
 */
public record SalarySummary(
        long headcount,
        String baseCurrency,
        BigDecimal totalSalary,
        BigDecimal averageSalary,
        BigDecimal medianSalary,
        BigDecimal minSalary,
        BigDecimal maxSalary
) {
}

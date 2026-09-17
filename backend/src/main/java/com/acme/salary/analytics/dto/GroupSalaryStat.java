package com.acme.salary.analytics.dto;

import java.math.BigDecimal;

/**
 * Compensation statistics for a single group (a country, department, or role).
 * Monetary values are in the base currency (USD).
 */
public record GroupSalaryStat(
        String group,
        long headcount,
        BigDecimal averageSalary,
        BigDecimal medianSalary,
        BigDecimal minSalary,
        BigDecimal maxSalary
) {
}

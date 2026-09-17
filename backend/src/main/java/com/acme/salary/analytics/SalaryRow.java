package com.acme.salary.analytics;

import java.math.BigDecimal;

/**
 * A minimal projection of the fields needed to compute pay analytics: the
 * grouping label, the local salary, and its currency. Fetching only these three
 * columns keeps the analytics query lean even though normalization to the base
 * currency must happen in the service (salaries are stored per-currency, so a
 * plain SQL AVG on baseSalary would incorrectly mix currencies).
 */
public record SalaryRow(String group, BigDecimal baseSalary, String currencyCode) {
}

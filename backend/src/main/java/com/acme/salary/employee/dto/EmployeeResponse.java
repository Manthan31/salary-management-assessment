package com.acme.salary.employee.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Read model for an employee returned by the API. Flattens reference data and
 * includes both the local salary and the USD-normalized salary so the UI can
 * display either without extra calls.
 */
public record EmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String country,
        String department,
        String jobRole,
        LocalDate hireDate,
        String status,
        BigDecimal baseSalary,
        String currencyCode,
        BigDecimal salaryInBaseCurrency
) {
}

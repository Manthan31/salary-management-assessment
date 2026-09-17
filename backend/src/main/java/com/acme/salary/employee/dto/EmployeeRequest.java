package com.acme.salary.employee.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Write model for creating or updating an employee. Reference entities are
 * referenced by id; validation enforces required fields and a positive salary.
 */
public record EmployeeRequest(
        @NotBlank(message = "firstName is required")
        String firstName,

        @NotBlank(message = "lastName is required")
        String lastName,

        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid address")
        String email,

        @NotNull(message = "countryId is required")
        Long countryId,

        @NotNull(message = "departmentId is required")
        Long departmentId,

        @NotNull(message = "jobRoleId is required")
        Long jobRoleId,

        @NotNull(message = "hireDate is required")
        LocalDate hireDate,

        @NotNull(message = "baseSalary is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "baseSalary must be positive")
        BigDecimal baseSalary
) {
}

package com.acme.salary.employee.dto;

import com.acme.salary.employee.EmploymentStatus;
import jakarta.validation.constraints.NotNull;

/** Request body for activating/deactivating an employee. */
public record StatusUpdateRequest(
        @NotNull(message = "status is required")
        EmploymentStatus status
) {
}

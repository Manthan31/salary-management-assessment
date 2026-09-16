package com.acme.salary.employee;

/**
 * Whether an employee currently contributes to headcount and pay analytics.
 * Deactivating rather than deleting preserves data integrity and history.
 */
public enum EmploymentStatus {
    ACTIVE,
    INACTIVE
}

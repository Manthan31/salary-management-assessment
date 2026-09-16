package com.acme.salary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Employee Salary Management backend.
 *
 * <p>Modular monolith (see docs/architecture.md): a single Spring Boot service
 * organized into cohesive packages (employee, analytics, reference, currency,
 * seed) with clean controller -> service -> repository layering.
 */
@SpringBootApplication
public class SalaryManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalaryManagementApplication.class, args);
    }
}

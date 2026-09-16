package com.acme.salary.employee;

import com.acme.salary.reference.Country;
import com.acme.salary.reference.Department;
import com.acme.salary.reference.JobRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * An employee and their current compensation.
 *
 * <p>Scope decision (see docs/requirements.md): we store the employee's
 * <em>current</em> salary only; historical salary tracking is out of scope.
 * Salaries are stored in the employee's local currency; analytics normalize
 * them to a base currency via {@code fx_rate}.
 *
 * <p>Indexes on the foreign keys and last name keep filtering, grouping, and
 * search fast at 10,000 rows.
 */
@Entity
@Table(name = "employee", indexes = {
        @Index(name = "idx_employee_country", columnList = "country_id"),
        @Index(name = "idx_employee_department", columnList = "department_id"),
        @Index(name = "idx_employee_job_role", columnList = "job_role_id"),
        @Index(name = "idx_employee_last_name", columnList = "last_name"),
        @Index(name = "idx_employee_status", columnList = "status")
})
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_role_id", nullable = false)
    private JobRole jobRole;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EmploymentStatus status = EmploymentStatus.ACTIVE;

    /** Annual base salary in the employee's local currency. */
    @Column(name = "base_salary", nullable = false, precision = 19, scale = 2)
    private BigDecimal baseSalary;

    /**
     * Currency the {@link #baseSalary} is denominated in (ISO 4217).
     * Denormalized from the country for query convenience and stability
     * (an employee keeps their pay currency even if reference data shifts).
     */
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    protected Employee() {
        // for JPA
    }

    public Employee(String firstName, String lastName, String email, Country country,
                    Department department, JobRole jobRole, LocalDate hireDate,
                    EmploymentStatus status, BigDecimal baseSalary, String currencyCode) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.country = country;
        this.department = department;
        this.jobRole = jobRole;
        this.hireDate = hireDate;
        this.status = status;
        this.baseSalary = baseSalary;
        this.currencyCode = currencyCode;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public JobRole getJobRole() {
        return jobRole;
    }

    public void setJobRole(JobRole jobRole) {
        this.jobRole = jobRole;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public EmploymentStatus getStatus() {
        return status;
    }

    public void setStatus(EmploymentStatus status) {
        this.status = status;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}

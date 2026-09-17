package com.acme.salary.analytics;

import com.acme.salary.employee.Employee;
import com.acme.salary.employee.EmploymentStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Read-only queries that pull the minimal salary projections needed for pay
 * analytics. Each method fetches only (group, salary, currency) rows, optionally
 * filtered, so the service can normalize to the base currency and aggregate.
 *
 * <p>Only ACTIVE employees are considered in analytics: they represent current
 * pay. Optional filters (country/department/role) narrow the population.
 */
public interface AnalyticsRepository extends JpaRepository<Employee, Long> {

    @Query("""
            select new com.acme.salary.analytics.SalaryRow(e.country.name, e.baseSalary, e.currencyCode)
            from Employee e
            where e.status = :status
              and (:country is null or e.country.name = :country)
              and (:department is null or e.department.name = :department)
              and (:role is null or e.jobRole.title = :role)
            """)
    List<SalaryRow> salaryRowsByCountry(@Param("status") EmploymentStatus status,
                                        @Param("country") String country,
                                        @Param("department") String department,
                                        @Param("role") String role);

    @Query("""
            select new com.acme.salary.analytics.SalaryRow(e.department.name, e.baseSalary, e.currencyCode)
            from Employee e
            where e.status = :status
              and (:country is null or e.country.name = :country)
              and (:department is null or e.department.name = :department)
              and (:role is null or e.jobRole.title = :role)
            """)
    List<SalaryRow> salaryRowsByDepartment(@Param("status") EmploymentStatus status,
                                           @Param("country") String country,
                                           @Param("department") String department,
                                           @Param("role") String role);

    @Query("""
            select new com.acme.salary.analytics.SalaryRow(e.jobRole.title, e.baseSalary, e.currencyCode)
            from Employee e
            where e.status = :status
              and (:country is null or e.country.name = :country)
              and (:department is null or e.department.name = :department)
              and (:role is null or e.jobRole.title = :role)
            """)
    List<SalaryRow> salaryRowsByRole(@Param("status") EmploymentStatus status,
                                     @Param("country") String country,
                                     @Param("department") String department,
                                     @Param("role") String role);

    /** All matching rows with a constant group label - used for org-wide summary and distribution. */
    @Query("""
            select new com.acme.salary.analytics.SalaryRow('ALL', e.baseSalary, e.currencyCode)
            from Employee e
            where e.status = :status
              and (:country is null or e.country.name = :country)
              and (:department is null or e.department.name = :department)
              and (:role is null or e.jobRole.title = :role)
            """)
    List<SalaryRow> allSalaryRows(@Param("status") EmploymentStatus status,
                                  @Param("country") String country,
                                  @Param("department") String department,
                                  @Param("role") String role);
}

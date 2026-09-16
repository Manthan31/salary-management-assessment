package com.acme.salary.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data access for employees.
 *
 * <p>Extends {@link JpaSpecificationExecutor} so the service layer can build
 * dynamic, composable filters (search + country/department/role) that translate
 * to a single indexed SQL query with server-side pagination - important for
 * staying responsive at 10,000 rows.
 */
public interface EmployeeRepository
        extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    boolean existsByEmail(String email);
}

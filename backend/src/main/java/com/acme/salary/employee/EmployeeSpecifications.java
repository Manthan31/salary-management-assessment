package com.acme.salary.employee;

import com.acme.salary.reference.Country;
import com.acme.salary.reference.Department;
import com.acme.salary.reference.JobRole;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Composable JPA {@link Specification}s for filtering employees. Combining these
 * produces a single indexed SQL query, so filtering stays efficient at 10,000
 * rows (we never load all rows into memory to filter in Java).
 */
public final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    /** Case-insensitive match of the term against first name, last name, or email. */
    public static Specification<Employee> search(String term) {
        if (!StringUtils.hasText(term)) {
            return null;
        }
        String like = "%" + term.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("firstName")), like),
                cb.like(cb.lower(root.get("lastName")), like),
                cb.like(cb.lower(root.get("email")), like)
        );
    }

    public static Specification<Employee> inCountry(String countryName) {
        if (!StringUtils.hasText(countryName)) {
            return null;
        }
        return (root, query, cb) -> {
            Join<Employee, Country> join = root.join("country");
            return cb.equal(join.get("name"), countryName);
        };
    }

    public static Specification<Employee> inDepartment(String departmentName) {
        if (!StringUtils.hasText(departmentName)) {
            return null;
        }
        return (root, query, cb) -> {
            Join<Employee, Department> join = root.join("department");
            return cb.equal(join.get("name"), departmentName);
        };
    }

    public static Specification<Employee> inRole(String roleTitle) {
        if (!StringUtils.hasText(roleTitle)) {
            return null;
        }
        return (root, query, cb) -> {
            Join<Employee, JobRole> join = root.join("jobRole");
            return cb.equal(join.get("title"), roleTitle);
        };
    }

    public static Specification<Employee> hasStatus(EmploymentStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}

package com.acme.salary.employee;

import com.acme.salary.common.DuplicateResourceException;
import com.acme.salary.common.PageResponse;
import com.acme.salary.common.ResourceNotFoundException;
import com.acme.salary.currency.CurrencyConverter;
import com.acme.salary.employee.dto.EmployeeRequest;
import com.acme.salary.employee.dto.EmployeeResponse;
import com.acme.salary.reference.Country;
import com.acme.salary.reference.CountryRepository;
import com.acme.salary.reference.Department;
import com.acme.salary.reference.DepartmentRepository;
import com.acme.salary.reference.JobRole;
import com.acme.salary.reference.JobRoleRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for managing employees: paginated/filtered listing, retrieval,
 * creation, update, and status changes. All aggregation-free reads and writes
 * live here; the controller stays thin.
 */
@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CountryRepository countryRepository;
    private final DepartmentRepository departmentRepository;
    private final JobRoleRepository jobRoleRepository;
    private final EmployeeMapper mapper;
    private final CurrencyConverter currencyConverter;

    public EmployeeService(EmployeeRepository employeeRepository,
                           CountryRepository countryRepository,
                           DepartmentRepository departmentRepository,
                           JobRoleRepository jobRoleRepository,
                           EmployeeMapper mapper,
                           CurrencyConverter currencyConverter) {
        this.employeeRepository = employeeRepository;
        this.countryRepository = countryRepository;
        this.departmentRepository = departmentRepository;
        this.jobRoleRepository = jobRoleRepository;
        this.mapper = mapper;
        this.currencyConverter = currencyConverter;
    }

    /**
     * Returns a page of employees matching the optional filters. Filtering and
     * pagination happen in the database via composed Specifications.
     */
    public PageResponse<EmployeeResponse> list(String search, String country, String department,
                                               String role, EmploymentStatus status, Pageable pageable) {
        Specification<Employee> spec = Specification
                .allOf(
                        EmployeeSpecifications.search(search),
                        EmployeeSpecifications.inCountry(country),
                        EmployeeSpecifications.inDepartment(department),
                        EmployeeSpecifications.inRole(role),
                        EmployeeSpecifications.hasStatus(status)
                );

        Page<Employee> page = employeeRepository.findAll(spec, pageable);

        // Load FX rates once, then map the whole page.
        Map<String, BigDecimal> rates = currencyConverter.loadRates();
        List<EmployeeResponse> content = page.getContent().stream()
                .map(e -> mapper.toResponse(e, rates))
                .toList();

        return PageResponse.from(page, content);
    }

    public EmployeeResponse get(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        return mapper.toResponse(employee);
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use: " + request.email());
        }
        Country country = findCountry(request.countryId());
        Department department = findDepartment(request.departmentId());
        JobRole jobRole = findJobRole(request.jobRoleId());

        Employee employee = new Employee(
                request.firstName(),
                request.lastName(),
                request.email(),
                country,
                department,
                jobRole,
                request.hireDate(),
                EmploymentStatus.ACTIVE,
                request.baseSalary(),
                country.getCurrencyCode()
        );
        return mapper.toResponse(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));

        // If the email changed, ensure the new one is not taken.
        if (!employee.getEmail().equalsIgnoreCase(request.email())
                && employeeRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use: " + request.email());
        }

        Country country = findCountry(request.countryId());
        Department department = findDepartment(request.departmentId());
        JobRole jobRole = findJobRole(request.jobRoleId());

        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setCountry(country);
        employee.setDepartment(department);
        employee.setJobRole(jobRole);
        employee.setHireDate(request.hireDate());
        employee.setBaseSalary(request.baseSalary());
        // Salary currency follows the (possibly new) country.
        employee.setCurrencyCode(country.getCurrencyCode());

        return mapper.toResponse(employee);
    }

    @Transactional
    public EmployeeResponse updateStatus(Long id, EmploymentStatus status) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        employee.setStatus(status);
        return mapper.toResponse(employee);
    }

    private Country findCountry(Long id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Country not found: " + id));
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
    }

    private JobRole findJobRole(Long id) {
        return jobRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job role not found: " + id));
    }
}

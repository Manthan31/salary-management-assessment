package com.acme.salary.employee;

import com.acme.salary.common.PageResponse;
import com.acme.salary.employee.dto.EmployeeRequest;
import com.acme.salary.employee.dto.EmployeeResponse;
import com.acme.salary.employee.dto.StatusUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for employee management. Thin layer: validates input, delegates
 * to {@link EmployeeService}, and shapes HTTP responses.
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private static final int MAX_PAGE_SIZE = 100;

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Paged, filterable employee list. All filtering and pagination is done in
     * the database.
     *
     * @param page   zero-based page index
     * @param size   page size (capped at {@value #MAX_PAGE_SIZE})
     * @param sort   field to sort by (whitelisted); defaults to lastName
     * @param search free-text match on name/email
     */
    @GetMapping
    public PageResponse<EmployeeResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) EmploymentStatus status) {

        Pageable pageable = buildPageable(page, size, sort, direction);
        return employeeService.list(search, country, department, role, status, pageable);
    }

    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable Long id) {
        return employeeService.get(id);
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse created = employeeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return employeeService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public EmployeeResponse updateStatus(@PathVariable Long id,
                                         @Valid @RequestBody StatusUpdateRequest request) {
        return employeeService.updateStatus(id, request.status());
    }

    /**
     * Builds a safe Pageable: caps size and only allows sorting on a whitelist
     * of fields to avoid arbitrary property injection.
     */
    private Pageable buildPageable(int page, int size, String sort, String direction) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        String safeSort = switch (sort) {
            case "firstName", "lastName", "email", "hireDate", "baseSalary", "id" -> sort;
            default -> "lastName";
        };
        Sort.Direction dir = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(safePage, safeSize, Sort.by(dir, safeSort));
    }
}

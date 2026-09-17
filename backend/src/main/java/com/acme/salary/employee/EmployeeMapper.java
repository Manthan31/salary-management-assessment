package com.acme.salary.employee;

import com.acme.salary.currency.CurrencyConverter;
import com.acme.salary.employee.dto.EmployeeResponse;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Maps {@link Employee} entities to API response DTOs, computing the
 * USD-normalized salary. Kept as a small component so it can be reused by the
 * service and unit-tested in isolation.
 */
@Component
public class EmployeeMapper {

    private final CurrencyConverter currencyConverter;

    public EmployeeMapper(CurrencyConverter currencyConverter) {
        this.currencyConverter = currencyConverter;
    }

    /** Maps a single employee, loading FX rates once for the conversion. */
    public EmployeeResponse toResponse(Employee employee) {
        return toResponse(employee, currencyConverter.loadRates());
    }

    /** Maps using a pre-loaded rate map - use when mapping a page of rows. */
    public EmployeeResponse toResponse(Employee employee, Map<String, BigDecimal> rates) {
        BigDecimal inBase = currencyConverter.toBase(
                employee.getBaseSalary(), employee.getCurrencyCode(), rates);
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getCountry().getName(),
                employee.getDepartment().getName(),
                employee.getJobRole().getTitle(),
                employee.getHireDate(),
                employee.getStatus().name(),
                employee.getBaseSalary(),
                employee.getCurrencyCode(),
                inBase
        );
    }
}

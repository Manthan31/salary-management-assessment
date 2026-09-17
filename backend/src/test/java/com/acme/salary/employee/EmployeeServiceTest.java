package com.acme.salary.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.acme.salary.common.DuplicateResourceException;
import com.acme.salary.common.ResourceNotFoundException;
import com.acme.salary.currency.CurrencyConverter;
import com.acme.salary.currency.FxRate;
import com.acme.salary.currency.FxRateRepository;
import com.acme.salary.employee.dto.EmployeeRequest;
import com.acme.salary.employee.dto.EmployeeResponse;
import com.acme.salary.reference.Country;
import com.acme.salary.reference.CountryRepository;
import com.acme.salary.reference.Department;
import com.acme.salary.reference.DepartmentRepository;
import com.acme.salary.reference.JobRole;
import com.acme.salary.reference.JobRoleRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link EmployeeService}. All repositories are mocked so the
 * tests run fast without a database and pin the service's business rules
 * (duplicate-email rejection, not-found handling, currency assignment).
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private CountryRepository countryRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private JobRoleRepository jobRoleRepository;
    @Mock private FxRateRepository fxRateRepository;

    private EmployeeService service;

    @BeforeEach
    void setUp() {
        CurrencyConverter converter = new CurrencyConverter(fxRateRepository);
        EmployeeMapper mapper = new EmployeeMapper(converter);
        service = new EmployeeService(employeeRepository, countryRepository,
                departmentRepository, jobRoleRepository, mapper, converter);
    }

    private void stubRates() {
        when(fxRateRepository.findAll()).thenReturn(List.of(
                new FxRate("USD", BigDecimal.ONE),
                new FxRate("EUR", new BigDecimal("1.10"))
        ));
    }

    private EmployeeRequest sampleRequest(String email) {
        return new EmployeeRequest("Ada", "Lovelace", email,
                1L, 2L, 3L, LocalDate.of(2020, 1, 15), new BigDecimal("100000"));
    }

    @Test
    void create_savesEmployeeWithCountryCurrency_andReturnsMappedResponse() {
        stubRates();
        Country germany = new Country("Germany", "DE", "EUR");
        when(employeeRepository.existsByEmail("ada@acme.example")).thenReturn(false);
        when(countryRepository.findById(1L)).thenReturn(Optional.of(germany));
        when(departmentRepository.findById(2L)).thenReturn(Optional.of(new Department("Engineering")));
        when(jobRoleRepository.findById(3L)).thenReturn(Optional.of(new JobRole("Software Engineer")));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeResponse response = service.create(sampleRequest("ada@acme.example"));

        assertThat(response.firstName()).isEqualTo("Ada");
        assertThat(response.country()).isEqualTo("Germany");
        // Currency follows the country (EUR), and 100000 EUR * 1.10 = 110000 USD.
        assertThat(response.currencyCode()).isEqualTo("EUR");
        assertThat(response.baseSalary()).isEqualByComparingTo("100000");
        assertThat(response.salaryInBaseCurrency()).isEqualByComparingTo("110000.00");
        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    void create_withDuplicateEmail_throwsAndDoesNotSave() {
        when(employeeRepository.existsByEmail("dup@acme.example")).thenReturn(true);

        assertThatThrownBy(() -> service.create(sampleRequest("dup@acme.example")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("dup@acme.example");
    }

    @Test
    void create_withMissingCountry_throwsNotFound() {
        when(employeeRepository.existsByEmail(any())).thenReturn(false);
        when(countryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(sampleRequest("x@acme.example")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Country not found");
    }

    @Test
    void get_missingEmployee_throwsNotFound() {
        when(employeeRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void updateStatus_changesStatus() {
        stubRates();
        Country usa = new Country("USA", "US", "USD");
        Employee employee = new Employee("Grace", "Hopper", "grace@acme.example",
                usa, new Department("Engineering"), new JobRole("Director"),
                LocalDate.of(2019, 3, 1), EmploymentStatus.ACTIVE,
                new BigDecimal("150000"), "USD");
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(employee));

        EmployeeResponse response = service.updateStatus(5L, EmploymentStatus.INACTIVE);

        assertThat(response.status()).isEqualTo("INACTIVE");
    }
}

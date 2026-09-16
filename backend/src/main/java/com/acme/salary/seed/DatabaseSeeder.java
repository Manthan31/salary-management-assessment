package com.acme.salary.seed;

import com.acme.salary.currency.FxRate;
import com.acme.salary.currency.FxRateRepository;
import com.acme.salary.employee.Employee;
import com.acme.salary.employee.EmployeeRepository;
import com.acme.salary.employee.EmploymentStatus;
import com.acme.salary.reference.Country;
import com.acme.salary.reference.CountryRepository;
import com.acme.salary.reference.Department;
import com.acme.salary.reference.DepartmentRepository;
import com.acme.salary.reference.JobRole;
import com.acme.salary.reference.JobRoleRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds the database with reference data, fixed FX rates, and a configurable
 * number of employees (default 10,000).
 *
 * <p>Determinism: a fixed random seed (app.seed.random-seed) drives all random
 * choices, so the generated dataset is identical on every run. This makes demos
 * and any data-dependent tests reproducible.
 *
 * <p>Idempotent: seeding is skipped if employees already exist, so restarting
 * the app does not duplicate data.
 */
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final CountryRepository countryRepository;
    private final DepartmentRepository departmentRepository;
    private final JobRoleRepository jobRoleRepository;
    private final FxRateRepository fxRateRepository;
    private final EmployeeRepository employeeRepository;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.employee-count:10000}")
    private int employeeCount;

    @Value("${app.seed.random-seed:42}")
    private long randomSeed;

    public DatabaseSeeder(CountryRepository countryRepository,
                          DepartmentRepository departmentRepository,
                          JobRoleRepository jobRoleRepository,
                          FxRateRepository fxRateRepository,
                          EmployeeRepository employeeRepository) {
        this.countryRepository = countryRepository;
        this.departmentRepository = departmentRepository;
        this.jobRoleRepository = jobRoleRepository;
        this.fxRateRepository = fxRateRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("Seeding disabled (app.seed.enabled=false); skipping.");
            return;
        }
        if (employeeRepository.count() > 0) {
            log.info("Database already contains employees; skipping seed.");
            return;
        }

        log.info("Seeding database: {} employees (random seed {}).", employeeCount, randomSeed);

        List<Country> countries = seedCountries();
        List<Department> departments = seedDepartments();
        List<JobRole> jobRoles = seedJobRoles();
        seedFxRates();
        seedEmployees(countries, departments, jobRoles);

        log.info("Seeding complete: {} employees.", employeeRepository.count());
    }

    private List<Country> seedCountries() {
        List<Country> countries = new ArrayList<>();
        for (SeedData.CountrySeed c : SeedData.COUNTRIES) {
            countries.add(new Country(c.name(), c.isoCode(), c.currencyCode()));
        }
        return countryRepository.saveAll(countries);
    }

    private List<Department> seedDepartments() {
        List<Department> departments = new ArrayList<>();
        for (String name : SeedData.DEPARTMENTS) {
            departments.add(new Department(name));
        }
        return departmentRepository.saveAll(departments);
    }

    private List<JobRole> seedJobRoles() {
        List<JobRole> roles = new ArrayList<>();
        for (String title : SeedData.JOB_ROLES) {
            roles.add(new JobRole(title));
        }
        return jobRoleRepository.saveAll(roles);
    }

    private void seedFxRates() {
        List<FxRate> rates = new ArrayList<>();
        for (SeedData.RateSeed r : SeedData.FX_RATES) {
            rates.add(new FxRate(r.currencyCode(), r.rateToBase()));
        }
        fxRateRepository.saveAll(rates);
    }

    private void seedEmployees(List<Country> countries, List<Department> departments,
                               List<JobRole> jobRoles) {
        Random random = new Random(randomSeed);
        List<Employee> batch = new ArrayList<>();
        int batchSize = 500;

        for (int i = 0; i < employeeCount; i++) {
            String firstName = pick(SeedData.FIRST_NAMES, random);
            String lastName = pick(SeedData.LAST_NAMES, random);
            // Email carries the row index to guarantee uniqueness across 10k rows.
            String email = (firstName + "." + lastName + (i + 1) + "@acme.example")
                    .toLowerCase();

            Country country = pick(countries, random);
            Department department = pick(departments, random);
            JobRole jobRole = pick(jobRoles, random);

            LocalDate hireDate = randomHireDate(random);
            EmploymentStatus status = random.nextInt(100) < 92
                    ? EmploymentStatus.ACTIVE
                    : EmploymentStatus.INACTIVE;

            BigDecimal salary = randomSalary(country.getCurrencyCode(), random);

            batch.add(new Employee(firstName, lastName, email, country, department,
                    jobRole, hireDate, status, salary, country.getCurrencyCode()));

            if (batch.size() == batchSize) {
                employeeRepository.saveAll(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            employeeRepository.saveAll(batch);
        }
    }

    private LocalDate randomHireDate(Random random) {
        // Hire dates spread across roughly the last 15 years.
        int daysAgo = random.nextInt(365 * 15);
        return LocalDate.of(2025, 1, 1).minusDays(daysAgo);
    }

    /**
     * Generates a plausible local-currency salary. The USD-equivalent band is
     * chosen first (so cross-country analytics are meaningful), then converted
     * into the local currency using the same fixed rates the seeder loads.
     */
    private BigDecimal randomSalary(String currencyCode, Random random) {
        // USD-equivalent annual salary between ~40k and ~200k.
        int usdEquivalent = 40_000 + random.nextInt(160_001);
        BigDecimal rateToBase = fxRateFor(currencyCode);
        // localAmount = usd / rateToBase
        return new BigDecimal(usdEquivalent)
                .divide(rateToBase, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal fxRateFor(String currencyCode) {
        for (SeedData.RateSeed r : SeedData.FX_RATES) {
            if (r.currencyCode().equals(currencyCode)) {
                return r.rateToBase();
            }
        }
        return BigDecimal.ONE;
    }

    private static <T> T pick(List<T> list, Random random) {
        return list.get(random.nextInt(list.size()));
    }
}

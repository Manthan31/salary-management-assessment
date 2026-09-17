package com.acme.salary.analytics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.acme.salary.analytics.dto.GroupSalaryStat;
import com.acme.salary.analytics.dto.SalaryBand;
import com.acme.salary.analytics.dto.SalarySummary;
import com.acme.salary.currency.CurrencyConverter;
import com.acme.salary.currency.FxRate;
import com.acme.salary.currency.FxRateRepository;
import com.acme.salary.employee.EmploymentStatus;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link AnalyticsService}. Uses a real {@link CurrencyConverter}
 * over a mocked FX repository so the important behavior - normalizing per-currency
 * salaries to USD *before* aggregating - is actually exercised, while the
 * analytics repository is mocked to return fixed rows (fast + deterministic).
 */
@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AnalyticsRepository analyticsRepository;

    @Mock
    private FxRateRepository fxRateRepository;

    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        CurrencyConverter converter = new CurrencyConverter(fxRateRepository);
        service = new AnalyticsService(analyticsRepository, converter);
        when(fxRateRepository.findAll()).thenReturn(List.of(
                new FxRate("USD", BigDecimal.ONE),
                new FxRate("EUR", new BigDecimal("1.10"))
        ));
    }

    @Test
    void summary_normalizesMixedCurrenciesBeforeAggregating() {
        // 100000 USD and 100000 EUR. EUR normalizes to 110000 USD.
        // Values in USD: [100000, 110000] -> avg 105000, median 105000.
        when(analyticsRepository.allSalaryRows(eq(EmploymentStatus.ACTIVE), any(), any(), any()))
                .thenReturn(List.of(
                        new SalaryRow("ALL", new BigDecimal("100000"), "USD"),
                        new SalaryRow("ALL", new BigDecimal("100000"), "EUR")
                ));

        SalarySummary summary = service.summary(null, null, null);

        assertThat(summary.headcount()).isEqualTo(2);
        assertThat(summary.baseCurrency()).isEqualTo("USD");
        assertThat(summary.totalSalary()).isEqualByComparingTo("210000.00");
        assertThat(summary.averageSalary()).isEqualByComparingTo("105000.00");
        assertThat(summary.medianSalary()).isEqualByComparingTo("105000.00");
        assertThat(summary.minSalary()).isEqualByComparingTo("100000.00");
        assertThat(summary.maxSalary()).isEqualByComparingTo("110000.00");
    }

    @Test
    void summary_ofNoEmployees_returnsZeros() {
        when(analyticsRepository.allSalaryRows(eq(EmploymentStatus.ACTIVE), any(), any(), any()))
                .thenReturn(List.of());

        SalarySummary summary = service.summary(null, null, null);

        assertThat(summary.headcount()).isZero();
        assertThat(summary.averageSalary()).isEqualByComparingTo("0.00");
        assertThat(summary.medianSalary()).isEqualByComparingTo("0.00");
    }

    @Test
    void byCountry_groupsAndSortsAlphabetically() {
        when(analyticsRepository.salaryRowsByCountry(eq(EmploymentStatus.ACTIVE), any(), any(), any()))
                .thenReturn(List.of(
                        new SalaryRow("Germany", new BigDecimal("100000"), "EUR"), // 110000 USD
                        new SalaryRow("USA", new BigDecimal("120000"), "USD"),
                        new SalaryRow("USA", new BigDecimal("80000"), "USD")
                ));

        List<GroupSalaryStat> stats = service.byCountry(null, null, null);

        assertThat(stats).hasSize(2);
        // Sorted alphabetically: Germany before USA.
        assertThat(stats.get(0).group()).isEqualTo("Germany");
        assertThat(stats.get(0).headcount()).isEqualTo(1);
        assertThat(stats.get(0).averageSalary()).isEqualByComparingTo("110000.00");

        assertThat(stats.get(1).group()).isEqualTo("USA");
        assertThat(stats.get(1).headcount()).isEqualTo(2);
        // avg of 120000 and 80000 = 100000
        assertThat(stats.get(1).averageSalary()).isEqualByComparingTo("100000.00");
    }

    @Test
    void distribution_partitionsSalariesIntoBands() {
        when(analyticsRepository.allSalaryRows(eq(EmploymentStatus.ACTIVE), any(), any(), any()))
                .thenReturn(List.of(
                        new SalaryRow("ALL", new BigDecimal("45000"), "USD"),   // 0-50k
                        new SalaryRow("ALL", new BigDecimal("60000"), "USD"),   // 50k-75k
                        new SalaryRow("ALL", new BigDecimal("250000"), "USD")   // 200k+
                ));

        List<SalaryBand> bands = service.distribution(null, null, null);

        long total = bands.stream().mapToLong(SalaryBand::headcount).sum();
        assertThat(total).isEqualTo(3);
        // Every employee falls into exactly one band (no loss, no double count).
        assertThat(bands.get(0).headcount()).isEqualTo(1); // 0-50k
        assertThat(bands.get(bands.size() - 1).headcount()).isEqualTo(1); // top open band
    }
}

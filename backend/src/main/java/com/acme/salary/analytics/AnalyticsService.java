package com.acme.salary.analytics;

import com.acme.salary.analytics.dto.GroupSalaryStat;
import com.acme.salary.analytics.dto.SalaryBand;
import com.acme.salary.analytics.dto.SalarySummary;
import com.acme.salary.currency.CurrencyConverter;
import com.acme.salary.employee.EmploymentStatus;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Computes compensation analytics ("how does the org pay people?").
 *
 * <p>Design note: salaries are stored per local currency, so every amount is
 * normalized to the base currency (USD) before aggregating. A plain SQL AVG on
 * baseSalary would incorrectly mix currencies. We therefore fetch a lean
 * (group, salary, currency) projection and aggregate in a single in-memory pass
 * using pure {@link SalaryStatistics} helpers - correct and easy to test.
 *
 * <p>Analytics consider ACTIVE employees only (current pay). Optional
 * country/department/role filters narrow the population.
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final CurrencyConverter currencyConverter;

    public AnalyticsService(AnalyticsRepository analyticsRepository,
                            CurrencyConverter currencyConverter) {
        this.analyticsRepository = analyticsRepository;
        this.currencyConverter = currencyConverter;
    }

    public SalarySummary summary(String country, String department, String role) {
        Map<String, BigDecimal> rates = currencyConverter.loadRates();
        List<BigDecimal> salaries = normalize(
                analyticsRepository.allSalaryRows(EmploymentStatus.ACTIVE, country, department, role), rates);

        return new SalarySummary(
                salaries.size(),
                CurrencyConverter.BASE_CURRENCY,
                SalaryStatistics.sum(salaries),
                SalaryStatistics.average(salaries),
                SalaryStatistics.median(salaries),
                SalaryStatistics.min(salaries),
                SalaryStatistics.max(salaries)
        );
    }

    public List<GroupSalaryStat> byCountry(String country, String department, String role) {
        return groupStats(analyticsRepository.salaryRowsByCountry(
                EmploymentStatus.ACTIVE, country, department, role));
    }

    public List<GroupSalaryStat> byDepartment(String country, String department, String role) {
        return groupStats(analyticsRepository.salaryRowsByDepartment(
                EmploymentStatus.ACTIVE, country, department, role));
    }

    public List<GroupSalaryStat> byRole(String country, String department, String role) {
        return groupStats(analyticsRepository.salaryRowsByRole(
                EmploymentStatus.ACTIVE, country, department, role));
    }

    /**
     * Salary distribution in fixed base-currency bands. Useful for a histogram
     * of "how many people earn in each pay range".
     */
    public List<SalaryBand> distribution(String country, String department, String role) {
        Map<String, BigDecimal> rates = currencyConverter.loadRates();
        List<BigDecimal> salaries = normalize(
                analyticsRepository.allSalaryRows(EmploymentStatus.ACTIVE, country, department, role), rates);

        List<SalaryBand> bands = new ArrayList<>();
        long[] edges = {0, 50_000, 75_000, 100_000, 150_000, 200_000};
        for (int i = 0; i < edges.length; i++) {
            BigDecimal lo = BigDecimal.valueOf(edges[i]);
            BigDecimal hi = (i + 1 < edges.length) ? BigDecimal.valueOf(edges[i + 1]) : null;
            long count = countInBand(salaries, lo, hi);
            String label = (hi != null)
                    ? formatK(edges[i]) + " - " + formatK(edges[i + 1])
                    : formatK(edges[i]) + "+";
            bands.add(new SalaryBand(label, lo, hi, count));
        }
        return bands;
    }

    // ---- helpers ----

    private List<GroupSalaryStat> groupStats(List<SalaryRow> rows) {
        Map<String, BigDecimal> rates = currencyConverter.loadRates();

        // Preserve a stable, readable ordering by group name.
        Map<String, List<BigDecimal>> byGroup = new LinkedHashMap<>();
        for (SalaryRow row : rows) {
            BigDecimal inBase = currencyConverter.toBase(row.baseSalary(), row.currencyCode(), rates);
            byGroup.computeIfAbsent(row.group(), k -> new ArrayList<>()).add(inBase);
        }

        List<GroupSalaryStat> stats = new ArrayList<>();
        for (Map.Entry<String, List<BigDecimal>> entry : byGroup.entrySet()) {
            List<BigDecimal> values = entry.getValue();
            stats.add(new GroupSalaryStat(
                    entry.getKey(),
                    values.size(),
                    SalaryStatistics.average(values),
                    SalaryStatistics.median(values),
                    SalaryStatistics.min(values),
                    SalaryStatistics.max(values)
            ));
        }
        stats.sort(Comparator.comparing(GroupSalaryStat::group));
        return stats;
    }

    private List<BigDecimal> normalize(List<SalaryRow> rows, Map<String, BigDecimal> rates) {
        return rows.stream()
                .map(r -> currencyConverter.toBase(r.baseSalary(), r.currencyCode(), rates))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private long countInBand(List<BigDecimal> salaries, BigDecimal lo, BigDecimal hi) {
        return salaries.stream()
                .filter(s -> s.compareTo(lo) >= 0 && (hi == null || s.compareTo(hi) < 0))
                .count();
    }

    private String formatK(long amount) {
        return "$" + (amount / 1000) + "k";
    }

    // Retained for potential reuse; groups a list by a key extractor.
    private <T> Map<String, List<T>> groupBy(List<T> items, Function<T, String> keyFn) {
        Map<String, List<T>> map = new LinkedHashMap<>();
        for (T item : items) {
            map.computeIfAbsent(keyFn.apply(item), k -> new ArrayList<>()).add(item);
        }
        return map;
    }
}

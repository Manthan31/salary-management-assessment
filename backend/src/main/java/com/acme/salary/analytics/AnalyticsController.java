package com.acme.salary.analytics;

import com.acme.salary.analytics.dto.GroupSalaryStat;
import com.acme.salary.analytics.dto.SalaryBand;
import com.acme.salary.analytics.dto.SalarySummary;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only endpoints that answer "how does the org pay people?" All monetary
 * values are normalized to the base currency (USD). Each endpoint accepts
 * optional country/department/role filters.
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public SalarySummary summary(@RequestParam(required = false) String country,
                                 @RequestParam(required = false) String department,
                                 @RequestParam(required = false) String role) {
        return analyticsService.summary(country, department, role);
    }

    @GetMapping("/by-country")
    public List<GroupSalaryStat> byCountry(@RequestParam(required = false) String country,
                                           @RequestParam(required = false) String department,
                                           @RequestParam(required = false) String role) {
        return analyticsService.byCountry(country, department, role);
    }

    @GetMapping("/by-department")
    public List<GroupSalaryStat> byDepartment(@RequestParam(required = false) String country,
                                              @RequestParam(required = false) String department,
                                              @RequestParam(required = false) String role) {
        return analyticsService.byDepartment(country, department, role);
    }

    @GetMapping("/by-role")
    public List<GroupSalaryStat> byRole(@RequestParam(required = false) String country,
                                        @RequestParam(required = false) String department,
                                        @RequestParam(required = false) String role) {
        return analyticsService.byRole(country, department, role);
    }

    @GetMapping("/distribution")
    public List<SalaryBand> distribution(@RequestParam(required = false) String country,
                                         @RequestParam(required = false) String department,
                                         @RequestParam(required = false) String role) {
        return analyticsService.distribution(country, department, role);
    }
}

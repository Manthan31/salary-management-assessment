package com.acme.salary.analytics.dto;

import java.math.BigDecimal;

/**
 * A salary distribution band and the number of employees whose base-currency
 * salary falls within it. {@code maxExclusive} is null for the open-ended top band.
 */
public record SalaryBand(
        String label,
        BigDecimal minInclusive,
        BigDecimal maxExclusive,
        long headcount
) {
}

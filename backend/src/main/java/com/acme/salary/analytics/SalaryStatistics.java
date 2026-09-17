package com.acme.salary.analytics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pure statistical helpers over base-currency salary amounts. Kept free of
 * Spring and persistence so the math (especially median) is trivial to unit
 * test and reason about.
 *
 * <p>All results are rounded to 2 decimal places (currency scale). The median
 * of an even-sized set is the average of the two middle values.
 */
public final class SalaryStatistics {

    private static final int SCALE = 2;

    private SalaryStatistics() {
    }

    public static BigDecimal sum(List<BigDecimal> values) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal v : values) {
            total = total.add(v);
        }
        return total.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal v : values) {
            total = total.add(v);
        }
        return total.divide(BigDecimal.valueOf(values.size()), SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Median value. For an even number of elements, returns the average of the
     * two central values. Returns zero for an empty list.
     */
    public static BigDecimal median(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        List<BigDecimal> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int n = sorted.size();
        int mid = n / 2;
        if (n % 2 == 1) {
            return sorted.get(mid).setScale(SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal lower = sorted.get(mid - 1);
        BigDecimal upper = sorted.get(mid);
        return lower.add(upper)
                .divide(BigDecimal.valueOf(2), SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal min(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal m = values.get(0);
        for (BigDecimal v : values) {
            if (v.compareTo(m) < 0) {
                m = v;
            }
        }
        return m.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal max(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal m = values.get(0);
        for (BigDecimal v : values) {
            if (v.compareTo(m) > 0) {
                m = v;
            }
        }
        return m.setScale(SCALE, RoundingMode.HALF_UP);
    }
}

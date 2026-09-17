package com.acme.salary.currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Converts salary amounts from an employee's local currency into the base
 * currency (USD) so that cross-country analytics are comparable.
 *
 * <p>Rates come from the fixed, seeded {@code fx_rate} table. The conversion
 * math is intentionally simple and pure ({@code amountInBase = local * rate})
 * so it is easy to reason about and unit test.
 */
@Service
public class CurrencyConverter {

    /** The currency all amounts are normalized to. */
    public static final String BASE_CURRENCY = "USD";

    private final FxRateRepository fxRateRepository;

    public CurrencyConverter(FxRateRepository fxRateRepository) {
        this.fxRateRepository = fxRateRepository;
    }

    /**
     * Loads all FX rates into a simple map for repeated conversions (e.g. when
     * normalizing a page of employees). Kept separate so callers can fetch once
     * and convert many times without hitting the DB per row.
     */
    public Map<String, BigDecimal> loadRates() {
        Map<String, BigDecimal> rates = new HashMap<>();
        List<FxRate> all = fxRateRepository.findAll();
        for (FxRate rate : all) {
            rates.put(rate.getCurrencyCode(), rate.getRateToBase());
        }
        // The base currency always converts 1:1, even if not explicitly stored.
        rates.putIfAbsent(BASE_CURRENCY, BigDecimal.ONE);
        return rates;
    }

    /**
     * Converts a local amount to the base currency using a pre-loaded rate map.
     *
     * @param localAmount  amount in the local currency (must not be null)
     * @param currencyCode ISO currency code of the local amount
     * @param rates        rate map from {@link #loadRates()}
     * @return the amount in base currency, rounded to 2 decimal places
     * @throws IllegalArgumentException if no rate is known for the currency
     */
    public BigDecimal toBase(BigDecimal localAmount, String currencyCode, Map<String, BigDecimal> rates) {
        if (localAmount == null) {
            throw new IllegalArgumentException("localAmount must not be null");
        }
        BigDecimal rate = rates.get(currencyCode);
        if (rate == null) {
            throw new IllegalArgumentException("No FX rate for currency: " + currencyCode);
        }
        return localAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Convenience single-value conversion. Prefer {@link #toBase(BigDecimal,
     * String, Map)} with a pre-loaded map when converting many amounts.
     */
    public BigDecimal toBase(BigDecimal localAmount, String currencyCode) {
        return toBase(localAmount, currencyCode, loadRates());
    }
}

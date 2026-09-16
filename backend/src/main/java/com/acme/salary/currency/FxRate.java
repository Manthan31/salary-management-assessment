package com.acme.salary.currency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * A fixed foreign-exchange rate used to normalize salaries to a single base
 * currency (USD) for cross-country analytics.
 *
 * <p>Rates are seeded and fixed (see docs/architecture.md): this keeps
 * analytics deterministic and avoids depending on an external FX API. The rate
 * is expressed as "how many base-currency units one unit of this currency is
 * worth" (i.e. amountInBase = localAmount * rateToBase).
 */
@Entity
@Table(name = "fx_rate")
public class FxRate {

    /** ISO 4217 currency code, e.g. "EUR". Natural primary key. */
    @Id
    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    /** Multiplier to convert one unit of this currency into the base currency. */
    @Column(name = "rate_to_base", nullable = false, precision = 19, scale = 6)
    private BigDecimal rateToBase;

    protected FxRate() {
        // for JPA
    }

    public FxRate(String currencyCode, BigDecimal rateToBase) {
        this.currencyCode = currencyCode;
        this.rateToBase = rateToBase;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public BigDecimal getRateToBase() {
        return rateToBase;
    }

    public void setRateToBase(BigDecimal rateToBase) {
        this.rateToBase = rateToBase;
    }
}

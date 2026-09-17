package com.acme.salary.currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for currency normalization. The FX repository is mocked so the
 * tests are fast and deterministic, and they pin the exact conversion math.
 */
@ExtendWith(MockitoExtension.class)
class CurrencyConverterTest {

    @Mock
    private FxRateRepository fxRateRepository;

    private CurrencyConverter converter;

    @BeforeEach
    void setUp() {
        converter = new CurrencyConverter(fxRateRepository);
    }

    @Test
    void loadRates_includesStoredRatesAndBaseCurrency() {
        when(fxRateRepository.findAll()).thenReturn(List.of(
                new FxRate("EUR", new BigDecimal("1.08")),
                new FxRate("INR", new BigDecimal("0.012"))
        ));

        Map<String, BigDecimal> rates = converter.loadRates();

        assertThat(rates.get("EUR")).isEqualByComparingTo("1.08");
        assertThat(rates.get("INR")).isEqualByComparingTo("0.012");
        // USD base is always present at 1:1 even if not stored.
        assertThat(rates.get("USD")).isEqualByComparingTo("1");
    }

    @Test
    void toBase_convertsLocalAmountUsingRate() {
        Map<String, BigDecimal> rates = Map.of(
                "USD", BigDecimal.ONE,
                "EUR", new BigDecimal("1.08"));

        // 1000 EUR * 1.08 = 1080.00 USD
        assertThat(converter.toBase(new BigDecimal("1000"), "EUR", rates))
                .isEqualByComparingTo("1080.00");
    }

    @Test
    void toBase_forBaseCurrency_isUnchanged() {
        Map<String, BigDecimal> rates = Map.of("USD", BigDecimal.ONE);
        assertThat(converter.toBase(new BigDecimal("50000"), "USD", rates))
                .isEqualByComparingTo("50000.00");
    }

    @Test
    void toBase_roundsToTwoDecimalPlaces() {
        Map<String, BigDecimal> rates = Map.of("JPY", new BigDecimal("0.0067"));
        // 123456 JPY * 0.0067 = 827.1552 -> 827.16
        assertThat(converter.toBase(new BigDecimal("123456"), "JPY", rates))
                .isEqualByComparingTo("827.16");
    }

    @Test
    void toBase_withUnknownCurrency_throws() {
        Map<String, BigDecimal> rates = Map.of("USD", BigDecimal.ONE);
        assertThatThrownBy(() -> converter.toBase(new BigDecimal("100"), "XYZ", rates))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("XYZ");
    }

    @Test
    void toBase_withNullAmount_throws() {
        Map<String, BigDecimal> rates = Map.of("USD", BigDecimal.ONE);
        assertThatThrownBy(() -> converter.toBase(null, "USD", rates))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

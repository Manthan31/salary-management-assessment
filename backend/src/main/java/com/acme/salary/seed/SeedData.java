package com.acme.salary.seed;

import java.math.BigDecimal;
import java.util.List;

/**
 * Static reference data used by the seeder.
 *
 * <p>All values are fixed constants so seeding is fully deterministic
 * (combined with a fixed random seed). FX rates are approximate,
 * illustrative values expressed as "how many USD one unit of the currency is
 * worth" (base currency = USD).
 */
final class SeedData {

    private SeedData() {
    }

    /** Base currency all analytics normalize to. */
    static final String BASE_CURRENCY = "USD";

    /** country name, ISO code, local currency. */
    record CountrySeed(String name, String isoCode, String currencyCode) {
    }

    /** currency code, rate to base (USD). */
    record RateSeed(String currencyCode, BigDecimal rateToBase) {
    }

    static final List<CountrySeed> COUNTRIES = List.of(
            new CountrySeed("United States", "US", "USD"),
            new CountrySeed("United Kingdom", "GB", "GBP"),
            new CountrySeed("Germany", "DE", "EUR"),
            new CountrySeed("France", "FR", "EUR"),
            new CountrySeed("India", "IN", "INR"),
            new CountrySeed("Canada", "CA", "CAD"),
            new CountrySeed("Australia", "AU", "AUD"),
            new CountrySeed("Japan", "JP", "JPY"),
            new CountrySeed("Brazil", "BR", "BRL"),
            new CountrySeed("Singapore", "SG", "SGD")
    );

    /** Fixed FX rates (currency -> USD). Deterministic; no external API. */
    static final List<RateSeed> FX_RATES = List.of(
            new RateSeed("USD", new BigDecimal("1.000000")),
            new RateSeed("GBP", new BigDecimal("1.270000")),
            new RateSeed("EUR", new BigDecimal("1.080000")),
            new RateSeed("INR", new BigDecimal("0.012000")),
            new RateSeed("CAD", new BigDecimal("0.730000")),
            new RateSeed("AUD", new BigDecimal("0.660000")),
            new RateSeed("JPY", new BigDecimal("0.0067000")),
            new RateSeed("BRL", new BigDecimal("0.180000")),
            new RateSeed("SGD", new BigDecimal("0.740000"))
    );

    static final List<String> DEPARTMENTS = List.of(
            "Engineering",
            "Sales",
            "Marketing",
            "Finance",
            "Human Resources",
            "Operations",
            "Customer Support",
            "Legal",
            "Product",
            "IT"
    );

    static final List<String> JOB_ROLES = List.of(
            "Software Engineer",
            "Senior Software Engineer",
            "Engineering Manager",
            "Sales Representative",
            "Sales Manager",
            "Marketing Specialist",
            "Financial Analyst",
            "HR Specialist",
            "Operations Coordinator",
            "Support Engineer",
            "Legal Counsel",
            "Product Manager",
            "Data Analyst",
            "System Administrator",
            "Director"
    );

    static final List<String> FIRST_NAMES = List.of(
            "James", "Mary", "John", "Patricia", "Robert", "Jennifer", "Michael", "Linda",
            "William", "Elizabeth", "David", "Barbara", "Richard", "Susan", "Joseph", "Jessica",
            "Thomas", "Sarah", "Charles", "Karen", "Christopher", "Nancy", "Daniel", "Lisa",
            "Matthew", "Margaret", "Anthony", "Betty", "Mark", "Sandra", "Priya", "Arjun",
            "Wei", "Yuki", "Hans", "Sofia", "Lucas", "Emma", "Noah", "Olivia"
    );

    static final List<String> LAST_NAMES = List.of(
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
            "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
            "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson",
            "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson", "Patel",
            "Sharma", "Chen", "Tanaka", "Muller", "Silva", "Nguyen", "Kim", "Singh", "Kumar"
    );
}

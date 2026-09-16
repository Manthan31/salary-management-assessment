package com.acme.salary.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * A country an employee can belong to. Each country has a default local
 * currency, which is what employee salaries in that country are denominated in.
 */
@Entity
@Table(name = "country", indexes = {
        @Index(name = "idx_country_name", columnList = "name")
})
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    /** ISO 3166-1 alpha-2 code, e.g. "US", "DE". */
    @Column(name = "iso_code", nullable = false, unique = true, length = 2)
    private String isoCode;

    /** ISO 4217 currency code for this country, e.g. "USD", "EUR". */
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    protected Country() {
        // for JPA
    }

    public Country(String name, String isoCode, String currencyCode) {
        this.name = name;
        this.isoCode = isoCode;
        this.currencyCode = currencyCode;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}

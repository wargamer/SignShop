package org.wargamer2010.signshop.money;

import java.util.Collections;
import java.util.List;

/**
 * Defines a currency that can be used on SignShop signs.
 * Configured in the {@code currencies:} section of config.yml.
 *
 * <p>Each currency maps recognized symbols/abbreviations on sign line 4 to a
 * Vault2 currency name. When VaultUnlocked is not available, only the default
 * currency is functional.</p>
 *
 * <p>The permission to create shops with a non-default currency is automatically
 * derived as {@code signshop.currency.create.<name>}. Default currency requires no permission.</p>
 */
public class CurrencyDefinition {

    /** Internal config key for this currency (e.g., "gems"). */
    private final String name;

    /**
     * Currency name passed to the Vault2 API (e.g., "gems").
     * Null or empty means use the legacy Vault economy (default currency).
     */
    private final String vault2Name;

    /** Symbols or abbreviations recognized on sign line 4 (case-insensitive). */
    private final List<String> symbols;

    /** If true, this is the default currency used when no symbol matches. */
    private final boolean isDefault;

    public CurrencyDefinition(String name, String vault2Name, List<String> symbols, boolean isDefault) {
        this.name = name;
        this.vault2Name = vault2Name;
        this.symbols = Collections.unmodifiableList(symbols);
        this.isDefault = isDefault;
    }

    public String getName() {
        return name;
    }

    /** The currency identifier passed to the Vault2 API. Null if this is the legacy default. */
    public String getVault2Name() {
        return vault2Name;
    }

    public List<String> getSymbols() {
        return symbols;
    }

    public boolean isDefault() {
        return isDefault;
    }

    /** The permission node required to create shops with this currency: {@code SignShop.Currency.Create.<name>}. */
    public String getPermission() {
        return "SignShop.Currency.Create." + name;
    }

    /** Returns true if a permission is required to create shops with this currency. Default currency has no restriction. */
    public boolean hasPermission() {
        return !isDefault;
    }

    /**
     * Returns true if this currency requires Vault2 multi-currency API.
     * False for the implicit default (legacy Vault).
     */
    public boolean requiresVault2() {
        return !isDefault && vault2Name != null && !vault2Name.isEmpty();
    }
}
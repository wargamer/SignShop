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

    /** Permission node required to create shops with this currency. Empty = no restriction. */
    private final String permission;

    public CurrencyDefinition(String name, String vault2Name, List<String> symbols,
                               boolean isDefault, String permission) {
        this.name = name;
        this.vault2Name = vault2Name;
        this.symbols = Collections.unmodifiableList(symbols);
        this.isDefault = isDefault;
        this.permission = permission == null ? "" : permission;
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

    /** The permission node required to create shops with this currency. Empty string = no restriction. */
    public String getPermission() {
        return permission;
    }

    /** Returns true if a permission is required to create shops with this currency. */
    public boolean hasPermission() {
        return !permission.isEmpty();
    }

    /**
     * Returns true if this currency requires Vault2 multi-currency API.
     * False for the implicit default (legacy Vault).
     */
    public boolean requiresVault2() {
        return vault2Name != null && !vault2Name.isEmpty();
    }
}
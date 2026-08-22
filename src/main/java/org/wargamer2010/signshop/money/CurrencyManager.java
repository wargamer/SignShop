package org.wargamer2010.signshop.money;

import org.bukkit.configuration.ConfigurationSection;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Vault;

import java.util.*;
import java.util.logging.Level;

/**
 * Manages multi-currency configuration for SignShop.
 *
 * <p>Loaded from the {@code currencies:} section of config.yml. Maps recognized
 * symbols and abbreviations from sign line 4 to {@link CurrencyDefinition} objects.
 * When no currencies section is configured, falls back to implicit single-currency
 * mode (legacy Vault, full backwards compatibility).</p>
 *
 * <p>Example config:</p>
 * <pre>
 * currencies:
 *   dollars:
 *     vault2-name: "dollars"
 *     symbols: ["$", "USD"]
 *     default: true
 *   gems:
 *     vault2-name: "gems"
 *     symbols: ["G", "gems"]
 *     default: false
 * </pre>
 * <p>Permissions are auto-derived: non-default currencies require {@code SignShop.Currency.Create.<name>}.</p>
 */
public class CurrencyManager {

    /**
     * The implicit default when no currencies: section is defined.
     * Has no vault2-name, so all operations use the legacy Vault economy.
     */
    public static final CurrencyDefinition IMPLICIT_DEFAULT =
            new CurrencyDefinition("default", null, Collections.emptyList(), true);

    private final Map<String, CurrencyDefinition> currencies = new LinkedHashMap<>();
    private CurrencyDefinition defaultCurrency = IMPLICIT_DEFAULT;

    /**
     * Loads currencies from the {@code currencies:} config section.
     * If section is null or empty, resets to implicit single-currency mode.
     */
    public void load(ConfigurationSection section) {
        currencies.clear();
        defaultCurrency = null;

        if (section == null) {
            defaultCurrency = IMPLICIT_DEFAULT;
            SignShop.log("CurrencyManager: no currencies: section found - single-currency mode (legacy Vault)", Level.INFO);
            return;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection curr = section.getConfigurationSection(key);
            if (curr == null) continue;

            String vault2Name = curr.getString("vault2-name", key);
            List<String> symbols = curr.getStringList("symbols");
            boolean isDefault = curr.getBoolean("default", false);

            CurrencyDefinition def = new CurrencyDefinition(key, vault2Name, symbols, isDefault);
            currencies.put(key.toLowerCase(), def);

            if (isDefault) {
                if (defaultCurrency != null) {
                    SignShop.log("Multiple currencies marked as default in config.yml - using first: "
                            + defaultCurrency.getName(), Level.WARNING);
                } else {
                    defaultCurrency = def;
                }
            }
        }

        if (defaultCurrency == null && !currencies.isEmpty()) {
            defaultCurrency = currencies.values().iterator().next();
            SignShop.log("No default currency configured in currencies: section - using '"
                    + defaultCurrency.getName() + "'", Level.WARNING);
        }

        if (defaultCurrency == null) {
            defaultCurrency = IMPLICIT_DEFAULT;
        }

        if (currencies.isEmpty()) {
            SignShop.log("CurrencyManager: currencies: section present but no valid entries loaded - single-currency mode (legacy Vault)", Level.WARNING);
        } else {
            SignShop.log("CurrencyManager: loaded " + currencies.size() + " currencies (default: " + defaultCurrency.getName() + ")", Level.INFO);
        }
    }

    /**
     * Matches non-numeric text extracted from a sign line against configured currency symbols.
     * Returns the default currency if no symbol matches.
     *
     * @param nonNumericText The non-numeric portion of the sign price line (e.g., "$", "G", "gems")
     * @return The matched {@link CurrencyDefinition}, or the default currency if no match
     */
    public CurrencyDefinition matchCurrency(String nonNumericText) {
        if (nonNumericText == null || nonNumericText.isBlank() || currencies.isEmpty()) {
            return defaultCurrency;
        }
        String trimmed = nonNumericText.trim().toLowerCase();
        if (trimmed.isEmpty()) return defaultCurrency;

        for (CurrencyDefinition def : currencies.values()) {
            for (String symbol : def.getSymbols()) {
                if (trimmed.equals(symbol.toLowerCase())) {
                    return def;
                }
            }
        }
        return defaultCurrency;
    }

    /** Returns the default currency (used when no symbol matches on the sign). */
    public CurrencyDefinition getDefaultCurrency() {
        return defaultCurrency;
    }

    /** Returns the currency with the given config key name, or null if not found. */
    public CurrencyDefinition getCurrency(String name) {
        if (name == null) return defaultCurrency;
        return currencies.get(name.toLowerCase());
    }

    /** Returns true if more than one currency is configured (multi-currency mode). */
    public boolean isMultiCurrencyEnabled() {
        return currencies.size() > 1;
    }

    /** Returns an unmodifiable view of all configured currencies. */
    public Collection<CurrencyDefinition> getCurrencies() {
        return Collections.unmodifiableCollection(currencies.values());
    }

    /**
     * Validates configured currencies against what the Vault2 economy supports.
     * Logs a warning for any vault2-name that does not exist in the economy.
     * Should be called after Vault2 detection completes.
     */
    public void validateWithVault2() {
        for (CurrencyDefinition def : currencies.values()) {
            if (!def.requiresVault2()) continue;
            Boolean hasCurrency = Vault.vault2HasCurrency(def.getVault2Name());
            if (hasCurrency == null) return; // vault2 not available
            if (!hasCurrency) {
                SignShop.log("Currency '" + def.getName() + "' uses vault2-name '"
                        + def.getVault2Name() + "' but this currency does not exist in the economy plugin!",
                        Level.WARNING);
            }
        }
    }
}
package org.wargamer2010.signshop;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Integration layer for Vault and VaultUnlocked APIs providing economy and permissions.
 *
 * <p>SignShop uses Vault as a soft dependency for money operations and permission checks.
 * When VaultUnlocked is available, SignShop can use the Vault2 Economy API for optimized
 * balance checking via the canAccept() method.</p>
 *
 * <p>Architecture:</p>
 * <ul>
 *   <li>Legacy Economy (net.milkbowl.vault.economy.Economy) - Used for deposit, withdraw, balance</li>
 *   <li>Vault2 Economy (net.milkbowl.vault2.economy.Economy) - Used for canAccept() when available</li>
 * </ul>
 *
 * @see net.milkbowl.vault.economy.Economy
 * @see net.milkbowl.vault.permission.Permission
 */
public class Vault {
    private static Permission permission = null;
    private static Economy economy = null;
    private static Chat chat = null;
    private static Boolean vaultFound = false;
    private static final Server server = Bukkit.getServer();
    private static final String nullString = null;

    // VaultUnlocked/Vault2 support
    private static Object vault2Economy = null;
    private static Method canAcceptMethod = null;
    private static boolean vaultUnlockedDetected = false;

    // Vault2 multi-currency methods (cached at startup)
    private static Method vault2HasWithCurrencyMethod = null;
    private static Method vault2WithdrawWithCurrencyMethod = null;
    private static Method vault2DepositWithCurrencyMethod = null;
    private static Method vault2FormatWithCurrencyMethod = null;
    private static Method vault2GetCurrenciesMethod = null;
    private static Method vault2HasCurrencyMethod = null;
    private static Method vault2HasMultiCurrencySupportMethod = null;
    private static Method vault2GetDefaultCurrencyMethod = null;
    private static boolean vault2MultiCurrencyAvailable = false;

    public Vault() {
        if (server.getPluginManager().isPluginEnabled("Vault")) {
            vaultFound = true;
        } else {
            SignShop.log("Vault not found - economy and permission features will not work!", Level.WARNING);
        }
    }

    /**
     * @return the permission provider
     */
    public static Permission getPermission() {
        return permission == null || permission.getName().equals("SuperPerms") ? null : permission;
    }

    /**
     * @return the economy provider
     */
    public static Economy getEconomy() {
        return economy;
    }

    /**
     * @return the chat provider
     */
    static Chat getChat() {
        return chat;
    }

    /**
     * @return the vaultFound
     */
    public static Boolean isVaultFound() {
        return vaultFound;
    }

    public static String getVersion() {
        if(isVaultFound()) {
            return server.getPluginManager().getPlugin("Vault").getDescription().getVersion();
        } else {
            return "N/A";
        }
    }

    /**
     * Returns true if the player has been removed from the given permission group successfully
     * First an attempt is made to remove the player from the global group with the given name
     * If that fails, the player is removed from the local group by the passed name
     *
     * @param player
     * @param group
     * @return
     */
    public static boolean removeGroupAnyWorld(Player player, String group) {
        Permission perm = getPermission();
        if(perm == null || !perm.hasGroupSupport())
            return false;
        return perm.playerRemoveGroup(nullString, player, group) || perm.playerRemoveGroup(player, group);
    }

    /**
     * Returns true if the player has been added to the given permission group successfully
     * First an attempt is made to add the player to the global group
     * If that fails, the player is added to the group in the current world
     *
     * @param player
     * @param group
     * @return
     */
    public static boolean addGroupAnyWorld(Player player, String group) {
        Permission perm = getPermission();
        if(perm == null || !perm.hasGroupSupport())
            return false;
        return perm.playerAddGroup(nullString, player, group) || perm.playerAddGroup(player, group);
    }

    Boolean setupPermissions() {
        if (!isVaultFound())
            return false;
        RegisteredServiceProvider<Permission> permissionProvider = server.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (getPermission() != null);
    }

    void setupChat() {
        if (!isVaultFound())
            return;
        RegisteredServiceProvider<Chat> chatProvider = server.getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

    }

    public Boolean setupEconomy() {
        if (!isVaultFound())
            return false;

        // Get legacy Economy provider (for standard operations)
        RegisteredServiceProvider<Economy> economyProvider = server.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        // Try to get Vault2 Economy provider (for canAccept)
        detectVault2Economy();

        return (getEconomy() != null);
    }

    /**
     * Detects VaultUnlocked's Vault2 Economy provider and canDeposit() method.
     * This is called once at startup to avoid per-call reflection overhead.
     */
    private void detectVault2Economy() {
        try {
            // Try to find net.milkbowl.vault2.economy.Economy service
            Class<?> vault2EconomyClass = Class.forName("net.milkbowl.vault2.economy.Economy");
            RegisteredServiceProvider<?> vault2Provider =
                server.getServicesManager().getRegistration(vault2EconomyClass);

            if (vault2Provider == null) {
                SignShop.log("Standard Vault economy detected (no Vault2 provider)", Level.INFO);
                return;
            }

            vault2Economy = vault2Provider.getProvider();

            // Check for canDeposit method on Vault2 provider
            Method method = vault2Economy.getClass().getMethod(
                "canDeposit", String.class, UUID.class, BigDecimal.class
            );

            // Test call to verify it's actually implemented (not just returning NOT_IMPLEMENTED)
            // Use a small positive amount since some economies reject zero/negative
            Object response = method.invoke(vault2Economy, "SignShop", UUID.randomUUID(), new BigDecimal("0.01"));

            // Get the response type - EconomyResponse.type is a public field, not a method
            java.lang.reflect.Field typeField = response.getClass().getField("type");
            Object responseType = typeField.get(response);

            if (responseType.toString().equals("NOT_IMPLEMENTED")) {
                canAcceptMethod = null;
                vaultUnlockedDetected = false;
                SignShop.log("Vault2 economy found but canDeposit() returns NOT_IMPLEMENTED - using standard Vault", Level.INFO);
            } else {
                canAcceptMethod = method;
                vaultUnlockedDetected = true;
                SignShop.log("VaultUnlocked Vault2 economy detected - using optimized balance checks", Level.INFO);
            }
        } catch (ClassNotFoundException e) {
            // Vault2 API not available - standard Vault only
            SignShop.log("Standard Vault economy detected", Level.INFO);
        } catch (NoSuchMethodException e) {
            SignShop.log("Vault2 economy found but no canDeposit() method - using standard Vault", Level.INFO);
        } catch (NoSuchFieldException e) {
            SignShop.log("Vault2 economy canDeposit() response missing type field - using standard Vault", Level.INFO);
        } catch (Exception e) {
            SignShop.log("Error detecting Vault2 economy: " + e.getMessage() + " - using standard Vault", Level.WARNING);
        }

        // Try to cache multi-currency methods even if canDeposit returned NOT_IMPLEMENTED
        if (vault2Economy != null) {
            detectVault2CurrencyMethods();
        }
    }

    /**
     * Attempts to cache Vault2 multi-currency method references for deposit, withdraw, has, format,
     * currencies(), hasCurrency(), hasMultiCurrencySupport(), and getDefaultCurrency().
     * Sets vault2MultiCurrencyAvailable to true only if all core transaction methods are found.
     */
    private void detectVault2CurrencyMethods() {
        try {
            Class<?> cls = vault2Economy.getClass();

            // Core transaction methods with currency parameter:
            // has(String, UUID, String, String, BigDecimal) - pluginName, accountID, world, currency, amount
            vault2HasWithCurrencyMethod = cls.getMethod("has",
                    String.class, UUID.class, String.class, String.class, BigDecimal.class);

            // withdraw(String, UUID, String, String, BigDecimal)
            vault2WithdrawWithCurrencyMethod = cls.getMethod("withdraw",
                    String.class, UUID.class, String.class, String.class, BigDecimal.class);

            // deposit(String, UUID, String, String, BigDecimal)
            vault2DepositWithCurrencyMethod = cls.getMethod("deposit",
                    String.class, UUID.class, String.class, String.class, BigDecimal.class);

            // Utility methods:
            // format(String, BigDecimal, String) - pluginName, amount, currency
            vault2FormatWithCurrencyMethod = cls.getMethod("format",
                    String.class, BigDecimal.class, String.class);

            // currencies() - returns Collection<String>
            vault2GetCurrenciesMethod = cls.getMethod("currencies");

            // hasCurrency(String) - returns boolean
            vault2HasCurrencyMethod = cls.getMethod("hasCurrency", String.class);

            // hasMultiCurrencySupport() - returns boolean
            vault2HasMultiCurrencySupportMethod = cls.getMethod("hasMultiCurrencySupport");

            // getDefaultCurrency(String) - returns String
            vault2GetDefaultCurrencyMethod = cls.getMethod("getDefaultCurrency", String.class);

            vault2MultiCurrencyAvailable = true;
            SignShop.log("VaultUnlocked Vault2 multi-currency methods detected - multi-currency support enabled", Level.INFO);

        } catch (NoSuchMethodException e) {
            vault2MultiCurrencyAvailable = false;
            SignShop.log("Vault2 economy found but currency-aware methods unavailable - multi-currency disabled", Level.INFO);
        } catch (Exception e) {
            vault2MultiCurrencyAvailable = false;
            SignShop.log("Error detecting Vault2 currency methods: " + e.getMessage(), Level.WARNING);
        }
    }

    /**
     * Check if player can accept a deposit using VaultUnlocked's Vault2 API.
     * This avoids the deposit/withdraw pattern that some economy plugins handle poorly.
     *
     * @param player The player to check
     * @param amount The amount to check
     * @return Boolean result (true = can accept deposit), or null if VaultUnlocked not available (use fallback)
     */
    public static Boolean canAcceptMoney(OfflinePlayer player, double amount) {
        if (canAcceptMethod == null || vault2Economy == null) {
            return null; // Signal to use fallback
        }
        try {
            Object response = canAcceptMethod.invoke(
                vault2Economy,
                "SignShop",
                player.getUniqueId(),
                BigDecimal.valueOf(amount)
            );
            Method successMethod = response.getClass().getMethod("transactionSuccess");
            return (Boolean) successMethod.invoke(response);
        } catch (Exception e) {
            SignShop.log("VaultUnlocked canDeposit() failed: " + e.getMessage(), Level.WARNING);
            return null; // Fall back to deposit/withdraw pattern
        }
    }

    /**
     * @return true if VaultUnlocked's Vault2 economy with canAccept() was detected
     */
    public static boolean isVaultUnlockedDetected() {
        return vaultUnlockedDetected;
    }

    /**
     * @return true if Vault2 multi-currency methods are available (deposit/withdraw/has with currency param)
     */
    public static boolean isVault2MultiCurrencyAvailable() {
        return vault2MultiCurrencyAvailable;
    }

    /**
     * Checks if the player has at least {@code amount} of the given currency using Vault2.
     *
     * @param accountID UUID of the player
     * @param currency  Vault2 currency name
     * @param amount    Amount to check
     * @return true if the player has enough, false if not, null if Vault2 is unavailable
     */
    public static Boolean vault2Has(UUID accountID, String currency, BigDecimal amount) {
        if (!vault2MultiCurrencyAvailable || vault2HasWithCurrencyMethod == null || vault2Economy == null)
            return null;
        try {
            return (Boolean) vault2HasWithCurrencyMethod.invoke(vault2Economy, "SignShop", accountID, null, currency, amount);
        } catch (Exception e) {
            SignShop.log("vault2Has() failed: " + e.getMessage(), Level.WARNING);
            return null;
        }
    }

    /**
     * Withdraws {@code amount} of the given currency from the player's account using Vault2.
     *
     * @param accountID UUID of the player
     * @param currency  Vault2 currency name
     * @param amount    Amount to withdraw (positive)
     * @return true on success, false on failure, null if Vault2 is unavailable
     */
    public static Boolean vault2Withdraw(UUID accountID, String currency, BigDecimal amount) {
        if (!vault2MultiCurrencyAvailable || vault2WithdrawWithCurrencyMethod == null || vault2Economy == null)
            return null;
        try {
            Object response = vault2WithdrawWithCurrencyMethod.invoke(vault2Economy, "SignShop", accountID, null, currency, amount);
            Method successMethod = response.getClass().getMethod("transactionSuccess");
            return (Boolean) successMethod.invoke(response);
        } catch (Exception e) {
            SignShop.log("vault2Withdraw() failed: " + e.getMessage(), Level.WARNING);
            return null;
        }
    }

    /**
     * Deposits {@code amount} of the given currency into the player's account using Vault2.
     *
     * @param accountID UUID of the player
     * @param currency  Vault2 currency name
     * @param amount    Amount to deposit (positive)
     * @return true on success, false on failure, null if Vault2 is unavailable
     */
    public static Boolean vault2Deposit(UUID accountID, String currency, BigDecimal amount) {
        if (!vault2MultiCurrencyAvailable || vault2DepositWithCurrencyMethod == null || vault2Economy == null)
            return null;
        try {
            Object response = vault2DepositWithCurrencyMethod.invoke(vault2Economy, "SignShop", accountID, null, currency, amount);
            Method successMethod = response.getClass().getMethod("transactionSuccess");
            return (Boolean) successMethod.invoke(response);
        } catch (Exception e) {
            SignShop.log("vault2Deposit() failed: " + e.getMessage(), Level.WARNING);
            return null;
        }
    }

    /**
     * Formats an amount in the given currency using Vault2's formatter.
     *
     * @param amount   Amount to format
     * @param currency Vault2 currency name
     * @return Formatted string, or null if Vault2 is unavailable
     */
    public static String vault2Format(BigDecimal amount, String currency) {
        if (!vault2MultiCurrencyAvailable || vault2FormatWithCurrencyMethod == null || vault2Economy == null)
            return null;
        try {
            return (String) vault2FormatWithCurrencyMethod.invoke(vault2Economy, "SignShop", amount, currency);
        } catch (Exception e) {
            SignShop.log("vault2Format() failed: " + e.getMessage(), Level.WARNING);
            return null;
        }
    }

    /**
     * Checks if the given currency exists in the Vault2 economy.
     *
     * @param currency Vault2 currency name to check
     * @return true if the currency exists, false if not, null if Vault2 is unavailable
     */
    public static Boolean vault2HasCurrency(String currency) {
        if (vault2Economy == null || vault2HasCurrencyMethod == null)
            return null;
        try {
            return (Boolean) vault2HasCurrencyMethod.invoke(vault2Economy, currency);
        } catch (Exception e) {
            SignShop.log("vault2HasCurrency() failed: " + e.getMessage(), Level.WARNING);
            return null;
        }
    }

    /**
     * Returns the default currency name from the Vault2 economy.
     *
     * @return The default currency name, or null if Vault2 is unavailable
     */
    public static String vault2GetDefaultCurrency() {
        if (vault2Economy == null || vault2GetDefaultCurrencyMethod == null)
            return null;
        try {
            return (String) vault2GetDefaultCurrencyMethod.invoke(vault2Economy, "SignShop");
        } catch (Exception e) {
            SignShop.log("vault2GetDefaultCurrency() failed: " + e.getMessage(), Level.WARNING);
            return null;
        }
    }

    /**
     * Returns true if the player is in a global or local group with the given name
     *
     * @param player
     * @param group
     * @return
     */
    public static boolean playerInGroupAnyWorld(Player player, String group) {
        Permission perm = getPermission();
        if(perm == null || !perm.hasGroupSupport())
            return false;
        return playerInGlobalGroup(player, group) || perm.playerInGroup(player, group);
    }

    /**
     * Returns true if the player is in a global group by the given name
     *
     * @param player
     * @param group
     * @return
     */
    public static boolean playerInGlobalGroup(Player player, String group) {
        Permission perm = getPermission();
        if(perm == null || !perm.hasGroupSupport())
            return false;
        return perm.playerInGroup(nullString, player, group);
    }

    /**
     * Returns the player's global group membership
     *
     * @param player
     * @return
     */
    public static String getGlobalPrimaryGroup(Player player) {
        Permission perm = getPermission();
        if(perm == null || !perm.hasGroupSupport())
            return null;
        return perm.getPrimaryGroup(nullString, player);
    }

    /**
     * Returns the player's global group memberships
     *
     * @param player
     * @return
     */
    public static String[] getGlobalGroups(Player player) {
        Permission perm = getPermission();
        if(perm == null || !perm.hasGroupSupport())
            return null;
        return perm.getPlayerGroups(nullString, player);
    }
}

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

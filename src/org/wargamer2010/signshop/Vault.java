package org.wargamer2010.signshop;

import java.util.logging.Level;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class Vault {
    private static Permission permission = null;
    private static Economy economy = null;
    private static Chat chat = null;
    private static Boolean vaultFound = false;
    private static Server server = Bukkit.getServer();
    private static final String nullString = null;

    public Vault() {
        if(server.getPluginManager().isPluginEnabled("Vault"))
            vaultFound = true;
        else
            SignShop.log("Vault plugin not enabled, SignShop can not run!", Level.SEVERE);
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
    public static Chat getChat() {
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

    public Boolean setupPermissions()
    {
        if(!isVaultFound())
            return false;
        RegisteredServiceProvider<Permission> permissionProvider = server.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (getPermission() != null);
    }

    public Boolean setupChat()
    {
        if(!isVaultFound())
            return false;
        RegisteredServiceProvider<Chat> chatProvider = server.getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return (getChat() != null);
    }

    public Boolean setupEconomy()
    {
        if(!isVaultFound())
            return false;
        RegisteredServiceProvider<Economy> economyProvider = server.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (getEconomy() != null);
    }

    /**
     * Returns true if the player has been removed from the given permission group succesfully
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
     * Returns true if the player has been added to the given permission group succesfully
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

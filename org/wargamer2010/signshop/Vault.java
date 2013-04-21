package org.wargamer2010.signshop;

import java.util.logging.Level;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.Server;

public class Vault {
    private static Permission permission = null;
    private static Economy economy = null;
    private static Chat chat = null;
    private static Boolean vaultFound = false;
    private static Server server = Bukkit.getServer();

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
        return permission;
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
}

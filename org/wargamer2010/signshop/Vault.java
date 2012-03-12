package org.wargamer2010.signshop;

import java.util.logging.Level;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.Server;

public class Vault {
    public static Permission permission = null;
    public static Economy economy = null;
    public static Chat chat = null;
    public static Boolean vaultFound = false;
    private Server server = null;

    public Vault(Server pServer) {
        if(pServer == null)
            return;
        server = pServer;
        if(server.getPluginManager().isPluginEnabled("Vault"))
            vaultFound = true;
        else
            SignShop.log("Vault plugin not enabled, SignShop can not run!", Level.SEVERE);
    }
    
    public Boolean setupPermissions()
    {
        if(!vaultFound)
            return false;
        RegisteredServiceProvider<Permission> permissionProvider = server.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    public Boolean setupChat()
    {
        if(!vaultFound)
            return false;
        RegisteredServiceProvider<Chat> chatProvider = server.getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return (chat != null);
    }

    public Boolean setupEconomy()
    {
        if(!vaultFound)
            return false;
        RegisteredServiceProvider<Economy> economyProvider = server.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
}

package org.wargamer2010.signshop;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

public class SignShopLoginListener implements Listener {
    private SignShop plugin;
    
    SignShopLoginListener(SignShop pPlugin) {
        plugin = pPlugin;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPluginEnabled(PlayerJoinEvent event) {
        if(event.getPlayer() == null)
            return;
        Player player = event.getPlayer();
        if(!Vault.vaultFound && player.isOp())
            player.sendMessage(plugin.getLogPrefix() + " Vault not found so plugin can not run. Please install Vault!");
    }
}

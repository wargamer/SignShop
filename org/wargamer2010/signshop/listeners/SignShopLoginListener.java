package org.wargamer2010.signshop.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class SignShopLoginListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnabled(PlayerJoinEvent event) {
        if(event.getPlayer() == null)
            return;
        Player player = event.getPlayer();
        if(!Vault.isVaultFound() && SignShopPlayer.isOp(player)) {
            player.sendMessage(SignShop.getLogPrefix()
                + " Vault not found so plugin can not run. Please install Vault!");
        }
        if(SignShopServerListener.isEssentialsConflictFound() && SignShopPlayer.isOp(player)) {
            player.sendMessage(SignShop.getLogPrefix()
                + " Essentials Signs are enabled that conflict with SignShop. Please check the log for more info!");
        }
    }
}

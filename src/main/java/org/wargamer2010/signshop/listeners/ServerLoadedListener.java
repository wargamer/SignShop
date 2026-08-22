package org.wargamer2010.signshop.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Vault;

import java.util.logging.Level;

/**
 * Listener that hooks into Vault's economy system after all plugins have loaded on server startup.
 */
public class ServerLoadedListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onServerLoaded(ServerLoadEvent event) {
        if (Vault.getEconomy() == null || !Vault.getEconomy().isEnabled()) {
            boolean economyHooked = SignShop.getInstance().getVault().setupEconomy();
            if (economyHooked) {
                SignShop.log("Vault economy successfully hooked!", Level.INFO);
            }
            else {
                SignShop.log("Could not hook into vault's economy. Make sure you have an economy plugin in addition to Vault", Level.WARNING);
            }
        }
    }
}

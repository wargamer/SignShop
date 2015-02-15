package org.wargamer2010.signshop.listeners;

import com.earth2me.essentials.Worth;
import java.io.File;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.wargamer2010.signshop.SignShop;

public class SignShopWorthListener implements Listener {
    private static final String essName = "Essentials";
    private static Plugin plEssentials = null;
    private static Worth wWorth = null;

    public SignShopWorthListener() {
        if(Bukkit.getServer().getPluginManager().isPluginEnabled(essName))
            itEnabled();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnabled(PluginEnableEvent event) {
        if(event.getPlugin().getName().equals(essName))
            itEnabled();
    }

    public final void itEnabled() {
        plEssentials = Bukkit.getServer().getPluginManager().getPlugin(essName);
        if(plEssentials == null)
            return;
        if(wWorth != null)
            wWorth.reloadConfig();
        else
            loadWorth();
    }

    public static double getPrice(ItemStack stack) {
        return wWorth.getPrice(stack).doubleValue();
    }

    public static boolean essLoaded() {
        return (plEssentials != null);
    }

    private void loadWorth() {
        if(plEssentials == null)
            return;
        File worthfile = new File(plEssentials.getDataFolder(), "worth.yml");
        if(!worthfile.exists()) {
            SignShop.log("Essentials was found but no worth.yml was found in it's plugin folder.", Level.WARNING);
            return;
        }
        wWorth = new Worth(plEssentials.getDataFolder());
    }
}

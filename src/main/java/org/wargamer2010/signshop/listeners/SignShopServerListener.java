package org.wargamer2010.signshop.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

/**
 * Listener that detects Essentials plugin and checks for sign conflicts on server startup and plugin enable.
 */
public class SignShopServerListener implements Listener {
    private final Server server;
    private static final String pluginName = "Essentials";

    public SignShopServerListener(Server pServer) {
        server = pServer;
        setupPluginToHookInto();
    }

    public static boolean isEssentialsConflictFound() {
        // Optional plugin integration - commented out for Folia build
        // if(Bukkit.getServer().getPluginManager().getPlugin(pluginName) != null)
        //     return EssentialsHelper.isEssentialsConflictFound();
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnabled(PluginEnableEvent event) {
        // Optional plugin integration - commented out for Folia build
        // if(event.getPlugin().getName().equals(pluginName))
        //     setupPluginToHookInto();
    }

    final public void setupPluginToHookInto() {
        // Optional plugin integration - commented out for Folia build
        // Plugin plugin = this.server.getPluginManager().getPlugin(pluginName);
        // if (plugin != null)
        //     EssentialsHelper.essentialsCheck(plugin);
    }
}

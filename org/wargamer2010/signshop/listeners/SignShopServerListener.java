package org.wargamer2010.signshop.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.Server;

public class SignShopServerListener implements Listener {
    private Server server;
    private static final String pluginName = "Essentials";

    public SignShopServerListener(Server pServer) {
        server = pServer;
        setupPluginToHookInto();
    }

    public static boolean isEssentialsConflictFound() {
        if(Bukkit.getServer().getPluginManager().getPlugin(pluginName) != null)
            return EssentialsHelper.isEssentialsConflictFound();
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnabled(PluginEnableEvent event) {
        if(event.getPlugin().getName().equals(pluginName))
            setupPluginToHookInto();
    }

    final public void setupPluginToHookInto() {
        Plugin plugin = this.server.getPluginManager().getPlugin(pluginName);
        if (plugin != null)
            EssentialsHelper.essentialsCheck(plugin);
    }
}

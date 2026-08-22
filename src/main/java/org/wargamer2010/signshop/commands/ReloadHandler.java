
package org.wargamer2010.signshop.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.api.Reloadable;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.logging.Level;

/**
 * Command handler for /signshop reload.
 * Reloads configuration files and dependent plugins without requiring server restart.
 */
public class ReloadHandler implements ICommandHandler {
    private static final ICommandHandler instance = new ReloadHandler();

    private ReloadHandler() {

    }

    public static ICommandHandler getInstance() {
        return instance;
    }

    @Override
    public boolean handle(String command, String[] args, SignShopPlayer player) {
        if (signshopUtil.notOPForCommand(player))
            return true;
        PluginManager pm = Bukkit.getPluginManager();

        SignShop.log("Reloading SignShop config and it's addons...", Level.INFO);
        if (player != null) {
            player.sendMessage(ChatColor.GREEN + "Reloading SignShop config and it's addons...");
        }

        SignShop.getInstance().reload();

        // Reload hard dependencies
        for(Plugin plugin : pm.getPlugins()) {
            if (plugin != null && plugin.getDescription() != null && plugin.isEnabled() && plugin.getDescription().getDepend() != null) {
                for (String depend : plugin.getDescription().getDepend()) {
                    if (depend.equalsIgnoreCase("signshop")) {
                        if (plugin instanceof Reloadable){
                            SignShop.log("Reloading " + plugin.getName(), Level.INFO);
                            Reloadable reloadableAddon = (Reloadable) plugin;
                            reloadableAddon.reload();
                        }else {
                            SignShop.log("You may also need to reload " + plugin.getName(), Level.INFO);
                        }
                        if (player != null) {
                            if (plugin instanceof Reloadable){
                                player.sendMessage(ChatColor.GREEN + "Reloaded " + plugin.getName());
                            }else {
                                player.sendMessage(ChatColor.GREEN + "You may also need to reload " + plugin.getName());
                            }
                        }
                    }
                }
            }
        }
        SignShop.log("Reload complete", Level.INFO);
        if (player != null) {
            player.sendMessage(ChatColor.GREEN + "SignShop has been reloaded");
        }
        return true;
    }

}

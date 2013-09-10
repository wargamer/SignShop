
package org.wargamer2010.signshop.commands;

import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.signshopUtil;

public class ReloadHandler implements ICommandHandler {
    private static ICommandHandler instance = new ReloadHandler();

    private ReloadHandler() {

    }

    public static ICommandHandler getInstance() {
        return instance;
    }

    @Override
    public boolean handle(String command, String[] args, SignShopPlayer player) {
        if(!signshopUtil.hasOPForCommand(player))
            return true;
        PluginManager pm = Bukkit.getPluginManager();

        pm.disablePlugin(SignShop.getInstance());
        pm.enablePlugin(SignShop.getInstance());

        // Reload hard dependencies
        for(Plugin plugin : pm.getPlugins()) {
            if(plugin != null && plugin.getDescription() != null && plugin.isEnabled() && plugin.getDescription().getDepend() != null) {
                for(String depend : plugin.getDescription().getDepend()) {
                    if(depend.equalsIgnoreCase("signshop")) {
                        pm.disablePlugin(plugin);
                        pm.enablePlugin(plugin);
                    }
                }
            }
        }

        SignShop.log("Reloaded", Level.INFO);
        if(player != null)
            player.sendMessage(ChatColor.GREEN + "SignShop has been reloaded");

        return true;
    }

}

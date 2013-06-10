
package org.wargamer2010.signshop.commands;

import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

        Bukkit.getServer().getPluginManager().disablePlugin(SignShop.getInstance());
        Bukkit.getServer().getPluginManager().enablePlugin(SignShop.getInstance());
        SignShop.log("Reloaded", Level.INFO);
        if(player != null)
            player.sendMessage(ChatColor.GREEN + "SignShop has been reloaded");

        return true;
    }

}

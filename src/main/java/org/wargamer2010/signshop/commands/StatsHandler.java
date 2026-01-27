
package org.wargamer2010.signshop.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.data.Storage;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.logging.Level;

/**
 * Command handler for /signshop stats.
 * Displays shop statistics, plugin versions, and system information.
 */
public class StatsHandler implements ICommandHandler {
    private static final ICommandHandler instance = new StatsHandler();

    private StatsHandler() {

    }

    public static ICommandHandler getInstance() {
        return instance;
    }

    @Override
    public boolean handle(String command, String[] args, SignShopPlayer player) {
        if (signshopUtil.notOPForCommand(player))
            return true;

        PluginDescriptionFile pdfFile = SignShop.getInstance().getDescription();
        String message = "Amount of Shops: " + Storage.get().shopCount() + "\n"
                + "SignShop version: " + pdfFile.getVersion() + "\n";

        PluginManager manager = Bukkit.getPluginManager();
        Plugin signshopHotel = manager.getPlugin("SignShopHotel");
        Plugin signshopGuardian = manager.getPlugin("SignShopGuardian");

        if(signshopHotel != null)
            message += "SignShop Hotel version: " + signshopHotel.getDescription().getVersion() + "\n";

        if(signshopGuardian != null)
            message += "SignShop Guardian version: " + signshopGuardian.getDescription().getVersion() + "\n";

        message = message
                + "Vault version: " + Vault.getVersion() + "\n"
                + "SignShop Authors: " + pdfFile.getAuthors().toString().replace("[", "").replace("]", "") + "\n"
                + "SignShop Home: http://tiny.cc/signshop3" + "\n";
        
        if(player != null)
            player.sendMessage(ChatColor.GREEN + message);
        else
            SignShop.log(message, Level.INFO);

        return true;
    }


}

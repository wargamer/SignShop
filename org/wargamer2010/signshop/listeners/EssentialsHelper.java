
package org.wargamer2010.signshop.listeners;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.ISettings;
import com.earth2me.essentials.signs.EssentialsSign;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.util.signshopUtil;

class EssentialsHelper {
    private static boolean essConflictFound = false;

    private EssentialsHelper() {

    }

    protected static boolean isEssentialsConflictFound() {
        return EssentialsHelper.essConflictFound;
    }

    protected static void essentialsCheck(Plugin plugin) {
        Essentials ess = (Essentials)plugin;
        if(ess != null) {
            ISettings settings = ess.getSettings();
            if(settings == null)
                return;
            if(!settings.areSignsDisabled()) {
                SignShop.log("Essentials signs are enabled, checking for conflicts now!", Level.WARNING);
                FileConfiguration config = ess.getConfig();
                if(config == null)
                    return;
                essConflictFound = false;
                for(EssentialsSign sign : ess.getSettings().enabledSigns()) {
                    String essShopName = signshopUtil.getOperation(sign.getTemplateName());
                    if(essShopName.isEmpty())
                        continue;
                    essShopName = ChatColor.stripColor(essShopName).toLowerCase();
                    if(!SignShopConfig.getBlocks(essShopName).isEmpty()) {
                        SignShop.log("Sign with name " + sign.getTemplateName() + " is enabled for Essentials and conflicts with SignShop!", Level.SEVERE);
                        if(!essConflictFound)
                            essConflictFound = true;
                    }
                }
            }
        }
    }
}

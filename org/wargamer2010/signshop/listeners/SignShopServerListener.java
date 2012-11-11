package org.wargamer2010.signshop.listeners;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.Server;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.ISettings;
import com.earth2me.essentials.signs.EssentialsSign;
import org.bukkit.ChatColor;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.util.signshopUtil;

public class SignShopServerListener implements Listener {
    private Server server;
    private Essentials ess = null;
    public static Boolean essConflictFound = false;

    public SignShopServerListener(Server pServer) {
        server = pServer;
        setupPluginToHookInto();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnabled(PluginEnableEvent event) {
        if(event.getPlugin().getName().equals("Essentials"))            
            setupPluginToHookInto();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisabled(PluginDisableEvent event) {
        if(event.getPlugin().getName().equals("Essentials"))
            this.ess = null;        
    }

    final public void setupPluginToHookInto() {
        Plugin plugin = this.server.getPluginManager().getPlugin("Essentials");

        if (plugin != null) {
            this.ess = (Essentials)plugin;
            essentialsCheck();
        }        
    }

    public void essentialsCheck() {
        if(this.ess != null) {
            ISettings settings = this.ess.getSettings();
            if(settings == null) {
                this.ess = null;
                return;
            }
            if(!settings.areSignsDisabled()) {
                SignShop.log("Essentials signs are enabled, checking for conflicts now!", Level.WARNING);
                FileConfiguration config = this.ess.getConfig();
                if(config == null) {
                    this.ess = null;
                    return;
                }       
                essConflictFound = false;
                for(EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
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

package me.specops.signshops;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.Server;
import java.util.logging.Level;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;

import org.bukkit.configuration.file.FileConfiguration;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.ISettings;

public class SignShopServerListener implements Listener {
    private Server server;
    private Essentials ess = null;

    SignShopServerListener(Server pServer) {
        server = pServer;
        setupPluginToHookInto();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPluginEnabled(PluginEnableEvent event) {
        Plugin plugin = this.server.getPluginManager().getPlugin("Essentials");

        if (this.ess == null) {
            if (plugin != null) {
                if (plugin.isEnabled()) {
                    this.ess = (Essentials)plugin;
                    essentialsCheck();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPluginDisabled(PluginDisableEvent event) {
        Plugin plugin = this.server.getPluginManager().getPlugin("Essentials");
        if (plugin != null)
            if (plugin.isEnabled())
                this.ess = null;
    }

    final public void setupPluginToHookInto() {
        Plugin plugin = this.server.getPluginManager().getPlugin("Essentials");

        if (this.ess == null) {
            if (plugin != null) {
                this.ess = (Essentials)plugin;
                essentialsCheck();
            }
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
                SignShop.log("Essentials signs are enabled, will conflict with SignShop", Level.WARNING);
                FileConfiguration config = this.ess.getConfig();
                if(config == null) {
                    this.ess = null;
                    return;
                }
                
                try {
                    FileReader reader = new FileReader(ess.getDataFolder() + "/config.yml");
                    if(reader == null)
                        return;
                    BufferedReader buffreader = new BufferedReader(reader);
                    String line;
                    File newFile = new File(ess.getDataFolder(), "config_2.yml");
                    if(!newFile.exists())
                        newFile.createNewFile();
                    FileWriter writer = new FileWriter(ess.getDataFolder() + "/config_2.yml");
                    boolean doComment = false;
                    while((line = buffreader.readLine()) != null) {
                        if(line.contains("signs-disabled"))
                            line = line.replace("false", "true");
                        if(doComment && line.contains(":"))
                            doComment = false;
                        if(line.contains("enabledSigns"))
                            doComment = true;
                        if(doComment && !line.contains("#") && line.contains("-"))
                            writer.write("# " + line + "\n");
                        else
                            writer.write(line + "\n");
                    }
                    writer.close();
                    reader.close();
                    File oldFile = new File(ess.getDataFolder(), "config.yml");
                    File bakFile = new File(ess.getDataFolder(), "config.bak");
                    if(bakFile.exists())
                        bakFile.delete();
                    oldFile.renameTo(bakFile);
                    if(!newFile.renameTo(oldFile))
                        return;
                } catch(IOException IOex) {
                    return;
                }
                SignShop.log("Disabled Essentials signs through Essentials' config.yml, reloading config now.", Level.INFO);
                this.ess.reload();
            }
        }
    }
}

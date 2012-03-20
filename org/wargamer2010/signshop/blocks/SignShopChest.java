package org.wargamer2010.signshop.blocks;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.plugin.Plugin;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.yi.acru.bukkit.Lockette.Lockette;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.lwc.LWC;

public class SignShopChest {
    Chest ssChest = null;    
    

    public SignShopChest(Block bChest) {
        if(bChest.getType() == Material.CHEST)
            ssChest = (Chest) bChest.getState();
    }
    
    public Boolean allowedToLink(SignShopPlayer ssPlayer) {
        Plugin plugin = null;        
        LWC lwc = null;
        Boolean bAllowed = true;

        plugin = Bukkit.getServer().getPluginManager().getPlugin("Lockette");
        if(plugin != null)
            bAllowed = (bAllowed ? (Lockette.isUser(ssChest.getBlock(), ssPlayer.getName(), false) || Lockette.isEveryone(ssChest.getBlock())) : bAllowed);
        plugin = Bukkit.getServer().getPluginManager().getPlugin("LWC");
        if(plugin != null) {
            lwc = ((LWCPlugin) plugin).getLWC();
            if(lwc != null) {                
                if(lwc.findProtection(ssChest.getBlock()) == null)
                    bAllowed = (bAllowed ? true : bAllowed);
                else
                    bAllowed = (bAllowed ? lwc.canAccessProtection(ssPlayer.getPlayer(), ssChest.getBlock()) : bAllowed);                
            }
        }
        
        return bAllowed;
    }
    
    
}

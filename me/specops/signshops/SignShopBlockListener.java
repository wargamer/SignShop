package me.specops.signshops;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.Material;

public class SignShopBlockListener implements Listener {
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event){
        if(event.getBlock().getType() == Material.WALL_SIGN
        || event.getBlock().getType() == Material.SIGN_POST){
            SignShop.Storage.removeSeller(event.getBlock().getLocation());
        }else if(event.getBlock().getType() == Material.CHEST){            
            //todo: remove signs when the chest is destroyed, need reverse lookup
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBurn(BlockBurnEvent event){
        if(event.getBlock().getType() == Material.WALL_SIGN
        || event.getBlock().getType() == Material.SIGN_POST){
            SignShop.Storage.removeSeller(event.getBlock().getLocation());
        }else if(event.getBlock().getType() == Material.CHEST){
            //todo: remove signs when the chest is destroyed, need reverse lookup
        }
    }
}

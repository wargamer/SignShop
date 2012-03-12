package org.wargamer2010.signshop;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.player.PlayerInventoryEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import java.util.List;
import org.bukkit.ChatColor;

public class SignShopBlockListener implements Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInventory(PlayerInventoryEvent event){
        event.getPlayer().sendMessage("LOL!");
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event){
        if(event.getBlock().getType() == Material.WALL_SIGN
        || event.getBlock().getType() == Material.SIGN_POST){
            SignShop.Storage.removeSeller(event.getBlock().getLocation());
        }else if(event.getBlock().getType() == Material.CHEST){            
            List<Block> signs = SignShop.Storage.getSignsFromChest(event.getBlock());
            if(signs != null)
                for (Block temp : signs) {
                    SignShop.Storage.removeSeller(temp.getLocation());
                    SignShopPlayerListener.setSignStatus(temp, ChatColor.BLACK);
                }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBurn(BlockBurnEvent event){
        if(event.getBlock().getType() == Material.WALL_SIGN
        || event.getBlock().getType() == Material.SIGN_POST){
            SignShop.Storage.removeSeller(event.getBlock().getLocation());
        }else if(event.getBlock().getType() == Material.CHEST){
            List<Block> signs = SignShop.Storage.getSignsFromChest(event.getBlock());
            if(signs != null)
                for (Block temp : signs)
                    SignShop.Storage.removeSeller(temp.getLocation());
        }
    }
}

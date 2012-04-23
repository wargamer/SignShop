package org.wargamer2010.signshop.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import java.util.List;
import org.bukkit.ChatColor;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Seller;

public class SignShopBlockListener implements Listener {
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event){
        
        if(event.getBlock().getType() == Material.WALL_SIGN
        || event.getBlock().getType() == Material.SIGN_POST){
            Seller seller = SignShop.Storage.getSeller(event.getBlock().getLocation());
            if(seller == null || (seller != null && (seller.owner.equals(event.getPlayer().getName()) || event.getPlayer().isOp())))
                SignShop.Storage.removeSeller(event.getBlock().getLocation());
            else
                event.setCancelled(true);
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

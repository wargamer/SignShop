package org.wargamer2010.signshop.specialops;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.List;
import org.bukkit.Material;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.*;

import com.miykeal.showCaseStandalone.*;
import org.bukkit.Bukkit;

public class linkShowcase implements SignShopSpecialOp {
    @Override
    public Boolean runOperation(List<Block> clickedBlocks, PlayerInteractEvent event) {
        Player player = event.getPlayer();
        SignShopPlayer ssPlayer = new SignShopPlayer(player);
        Block shopSign = event.getClickedBlock();
        Seller seller = SignShop.Storage.getSeller(shopSign.getLocation());
        if(seller == null)
            return false;
        if(seller.getContainables().isEmpty())
            return false;
        if(!itemUtil.clickedSign(shopSign))
            return false;
        
        Block bStep = null;
        for(Block bTemp : clickedBlocks) {
            if(bTemp.getType() == Material.STEP)
                bStep = bTemp;
        }
        if(bStep == null)
            return false;
        
        ItemStack showcasing = null;
        for(Block bHolder : seller.getContainables()) {
            if(bHolder.getState() instanceof InventoryHolder) {
                InventoryHolder Holder = (InventoryHolder)bHolder.getState();
                for(ItemStack isTemp : Holder.getInventory().getContents()) {
                    if(isTemp != null) {
                        showcasing = isTemp;
                        break;
                    }
                }
            }
            if(showcasing != null)
                break;
        }
        if(showcasing == null) {
            ssPlayer.sendMessage(SignShop.Errors.get("chest_empty"));
            return false;
        }
        
        if(Bukkit.getServer().getPluginManager().getPlugin("ShowCaseStandalone") == null)
            return false;
        ShowCaseStandalone scs = (ShowCaseStandalone) Bukkit.getServer().getPluginManager().getPlugin("ShowCaseStandalone");
        com.miykeal.showCaseStandalone.Shops.Shop p = new com.miykeal.showCaseStandalone.Shops.DisplayShop(scs);
        p.setItemStack(showcasing);
        p.setLocation ( bStep.getLocation() );
        p.setBlock ( bStep );
        try {
            p.setSHA1(com.miykeal.showCaseStandalone.Utilities.Utilities.sha1(bStep.toString()));
        } catch(java.io.IOException ex) {
            // TODO: Might have to log this if it happens
        }
        scs.getShopHandler().addShop (p);
        try {
            scs.getShopHandler().saveAll();
        } catch(java.io.IOException ex) {
            // TODO: Might have to log this if it happens
        }
        
        scs.getShopHandler().showAll();
        seller.getMisc().put("showcaselocation", signshopUtil.convertLocationToString(bStep.getLocation()));
        return true;
    }
}

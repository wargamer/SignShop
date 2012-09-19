package org.wargamer2010.signshop.specialops;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.List;
import java.lang.reflect.*;
import org.bukkit.Material;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.*;

import com.miykeal.showCaseStandalone.*;
import java.util.logging.Level;
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
        if(seller.getItems() == null || seller.getItems().length == 0) {
            ssPlayer.sendMessage(SignShop.Errors.get("chest_empty"));
            return false;
        }
        showcasing = seller.getItems()[0];
        
        if(Bukkit.getServer().getPluginManager().getPlugin("ShowCaseStandalone") == null)
            return false;
        ShowCaseStandalone scs = (ShowCaseStandalone) Bukkit.getServer().getPluginManager().getPlugin("ShowCaseStandalone");
        com.miykeal.showCaseStandalone.ShopInternals.Storage storage = null;
        try {
            try {
                Constructor con = com.miykeal.showCaseStandalone.ShopInternals.Storage.class.getConstructor(new Class[]{int.class});
                storage = (com.miykeal.showCaseStandalone.ShopInternals.Storage)con.newInstance(1);
            } catch(NoSuchMethodException ex) {
                try {
                    Constructor con = com.miykeal.showCaseStandalone.ShopInternals.Storage.class.getConstructor(new Class[]{});
                    storage = (com.miykeal.showCaseStandalone.ShopInternals.Storage)con.newInstance();
                } catch(NoSuchMethodException ex2) {
                    storage = null;
                    return true;
                }

            }
        } catch(InstantiationException inst) { storage = null; }
        catch(IllegalAccessException ill) { storage = null; }
        catch(InvocationTargetException inc) { storage = null; }
        if(storage == null) {
            SignShop.log("Invalid version of ShowCaseStandalone detected, please get the latest!", Level.WARNING);
            return true;
        }
        
        com.miykeal.showCaseStandalone.Shops.Shop p = new com.miykeal.showCaseStandalone.Shops.DisplayShop(scs, storage);
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

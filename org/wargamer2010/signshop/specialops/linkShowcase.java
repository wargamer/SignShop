package org.wargamer2010.signshop.specialops;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.List;
import java.lang.reflect.*;
import org.bukkit.Material;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.*;

import com.kellerkindt.scs.*;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.wargamer2010.signshop.configuration.SignShopConfig;

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
            if(bTemp.getType() == Material.getMaterial("STEP"))
                bStep = bTemp;
        }
        if(bStep == null)
            return false;

        ItemStack showcasing = null;
        if(seller.getItems() == null || seller.getItems().length == 0 || seller.getItems()[0] == null) {
            ssPlayer.sendMessage(SignShopConfig.getError(("chest_empty"), null));
            return false;
        }
        showcasing = seller.getItems()[0];

        if(Bukkit.getServer().getPluginManager().getPlugin("ShowCaseStandalone") == null)
            return false;
        ShowCaseStandalone scs = (ShowCaseStandalone) Bukkit.getServer().getPluginManager().getPlugin("ShowCaseStandalone");
        com.kellerkindt.scs.internals.Storage storage = new com.kellerkindt.scs.internals.Storage(1, Integer.toString(bStep.hashCode()));
        if(storage == null) {
            SignShop.log("Invalid version of ShowCaseStandalone detected, please get the latest!", Level.WARNING);
            return true;
        }

        com.kellerkindt.scs.shops.Shop p = new com.kellerkindt.scs.shops.DisplayShop(scs, storage);
        p.setItemStack(showcasing);
        p.setLocation(bStep.getLocation());
        p.setBlock(bStep);
        scs.getShopHandler().addShop(p);
        scs.getShopHandler().showAll();
        seller.getMisc().put("showcaselocation", signshopUtil.convertLocationToString(bStep.getLocation()));
        SignShop.log("Showcase has been successfully created.", Level.WARNING);
        return true;
    }
}

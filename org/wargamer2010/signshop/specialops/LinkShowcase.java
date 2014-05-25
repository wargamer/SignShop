package org.wargamer2010.signshop.specialops;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.List;
import org.bukkit.Material;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.*;

import com.kellerkindt.scs.*;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.player.PlayerIdentifier;

public class LinkShowcase implements SignShopSpecialOp {
    @Override
    public Boolean runOperation(List<Block> clickedBlocks, PlayerInteractEvent event, Boolean ranSomething) {
        Player player = event.getPlayer();
        SignShopPlayer ssPlayer = new SignShopPlayer(player);
        Block shopSign = event.getClickedBlock();
        Seller seller = Storage.get().getSeller(shopSign.getLocation());
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

        ItemStack showcasing;
        if(seller.getItems() == null || seller.getItems().length == 0 || seller.getItems()[0] == null) {
            ssPlayer.sendMessage(SignShopConfig.getError(("chest_empty"), null));
            return false;
        }
        showcasing = seller.getItems()[0];

        if(Bukkit.getServer().getPluginManager().getPlugin("ShowCaseStandalone") == null)
            return false;
        if(!PlayerIdentifier.GetUUIDSupport()) {
            SignShop.log("No UUID support detected but ShowCaseStandalone requires it. Please downgrade ShowCaseStandalone or upgrade Bukkit.", Level.WARNING);
            return false;
        }

        ShowCaseStandalone scs = (ShowCaseStandalone) Bukkit.getServer().getPluginManager().getPlugin("ShowCaseStandalone");

        com.kellerkindt.scs.shops.Shop p = new com.kellerkindt.scs.shops.DisplayShop(bStep.getWorld().getUID(),
                ssPlayer.getPlayer().getUniqueId(), bStep.getLocation(), showcasing);
        p.setItemStack(showcasing);
        p.setAmount(1);
        p.setVisible(true);
        scs.getShopHandler().addShop(p);
        scs.getShopHandler().showAll();
        seller.addMisc("showcaselocation", signshopUtil.convertLocationToString(bStep.getLocation()));
        ssPlayer.sendMessage("Showcase has been successfully created.");
        return true;
    }
}

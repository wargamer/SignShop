package org.wargamer2010.signshop.specialops;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.*;

import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;

public class LinkItemFrame implements SignShopSpecialOp {
    @Override
    public Boolean runOperation(List<Block> clickedBlocks, PlayerInteractEvent event, Boolean ranSomething) {
        if(Material.getMaterial("ITEM_FRAME") == null)
            return false; // No support for itemframes
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

        ItemFrame itemframe = null;
        for(Map.Entry<Entity, Player> entry : clicks.mClicksPerEntity.entrySet()) {
            if(entry.getValue() == player && entry.getKey() instanceof ItemFrame)
                itemframe = (ItemFrame)entry.getKey();
        }
        if(itemframe == null)
            return false;

        ItemStack showcasing;
        if(seller.getItems() == null || seller.getItems().length == 0 || seller.getItems()[0] == null) {
            ssPlayer.sendMessage(SignShopConfig.getError(("chest_empty"), null));
            return false;
        }
        showcasing = seller.getItems()[0];
        clicks.removePlayerFromEntityMap(player);

        itemframe.setItem(showcasing);

        seller.getMisc().put("itemframelocation", signshopUtil.convertLocationToString(itemframe.getLocation()));
        ssPlayer.sendMessage("ItemFrame has been successfully linked.");
        return true;
    }
}

package org.wargamer2010.signshop.specialops;

import java.util.LinkedList;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.*;

import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.operations.SignShopArguments;

public class LinkItemFrame implements SignShopSpecialOp {
    private static final String MiscSetting = "itemframelocation";

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


        List<ItemFrame> itemframes = new LinkedList<ItemFrame>();
        for(Map.Entry<Entity, Player> entry : clicks.mClicksPerEntity.entrySet()) {
            if(entry.getValue() == player && entry.getKey() instanceof ItemFrame)
                itemframes.add((ItemFrame)entry.getKey());
        }
        if(itemframes.isEmpty())
            return false;

        ItemStack showcasing;
        if(seller.getItems() == null || seller.getItems().length == 0 || seller.getItems()[0] == null) {
            ssPlayer.sendMessage(SignShopConfig.getError(("chest_empty"), null));
            return false;
        }
        showcasing = seller.getItems()[0];
        clicks.removePlayerFromEntityMap(player);

        List<Entity> newEntities = new LinkedList<Entity>();
        List<Entity> currentEntities = signshopUtil.getEntitiesFromMisc(seller, MiscSetting);
        Boolean bUnlinked = false;
        for(ItemFrame bTemp : itemframes) {
            if(currentEntities.contains(bTemp)) {
                ssPlayer.sendMessage(SignShopConfig.getError("ItemFrame has been successfully unlinked.", null));
                bUnlinked = true;
                currentEntities.remove(bTemp);
                bTemp.setItem(null);
            } else {
                newEntities.add(bTemp);
                bTemp.setItem(showcasing);
            }
        }

        if(!bUnlinked && newEntities.isEmpty())
            return false;
        else if(newEntities.isEmpty()) {
            seller.getMisc().remove(MiscSetting);
            Storage.get().SafeSave();
            return true;
        }
        currentEntities.addAll(newEntities);

        List<String> entityLocations = new LinkedList<String>();
        for(Entity ent : currentEntities)
            entityLocations.add(signshopUtil.convertLocationToString(ent.getLocation()));
        String[] implodedLocations = new String[entityLocations.size()];
        entityLocations.toArray(implodedLocations);
        String locations = signshopUtil.implode(implodedLocations, SignShopArguments.seperator);

        if(locations.isEmpty())
            return true;

        ssPlayer.sendMessage("ItemFrame has been successfully linked.");
        seller.getMisc().put(MiscSetting, locations);
        Storage.get().SafeSave();

        // seller.getMisc().put("itemframelocation", signshopUtil.convertLocationToString(itemframe.getLocation()));
        // ssPlayer.sendMessage("ItemFrame has been successfully linked.");
        return true;
    }
}

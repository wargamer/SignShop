package org.wargamer2010.signshop.operations;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.util.itemUtil;

public class playJukebox implements SignShopOperation {
    private ItemStack[] getRecords(List<Block> containables) {
        List<ItemStack> tempItems = new ArrayList<ItemStack>();
        ItemStack[] isTotalItems;

        for(Block bHolder : containables) {
            if(bHolder.getState() instanceof InventoryHolder) {
                InventoryHolder Holder = (InventoryHolder)bHolder.getState();
                for(ItemStack item : Holder.getInventory().getContents()) {
                    if(item != null && item.getAmount() > 0 && item.getType().isRecord()) {
                        tempItems.add(item);
                    }
                }
            }
        }
        isTotalItems = tempItems.toArray(new ItemStack[tempItems.size()]);
        return isTotalItems;
    }

    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(ssArgs.getContainables().isEmpty()) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("chest_missing", ssArgs.getMessageParts()));
            return false;
        }
        ItemStack[] isTotalItems = getRecords(ssArgs.getContainables().get());

        if(isTotalItems.length == 0) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("chest_empty", ssArgs.getMessageParts()));
            return false;
        }
        ssArgs.getItems().set(isTotalItems);
        ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.getItems().get()));
        return true;
    }


    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        ItemStack[] isTotalItems = getRecords(ssArgs.getContainables().get());

        if(isTotalItems.length == 0) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("chest_empty", ssArgs.getMessageParts()));
            return false;
        }

        return true;
    }

    private void playEffect(SignShopArguments ssArgs, Material type) {
        ssArgs.getSign().get().getWorld().playEffect(ssArgs.getSign().get().getLocation(), Effect.RECORD_PLAY, type);
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ItemStack[] isTotalItems = getRecords(ssArgs.getContainables().get());
        Seller seller = Storage.get().getSeller(ssArgs.getSign().get().getLocation());
        String sLastrecord = seller.getVolatile("lastrecord");
        Boolean doNext = false;
        Integer counter = 0;
        ItemStack firstItem = isTotalItems[0];
        for(ItemStack item : isTotalItems) {
            counter++;
            if(sLastrecord == null || doNext == true) {
                playEffect(ssArgs, item.getType());
                sLastrecord = item.getType().toString();
                if(sLastrecord == null)
                    doNext = true;
                break;
            }
            if(item.getType().toString().equals(sLastrecord))
                doNext = true;
            if(doNext == true && counter == isTotalItems.length) {
                playEffect(ssArgs, firstItem.getType());
                sLastrecord = firstItem.getType().toString();
                break;
            }
        }
        if(doNext == false) {
            playEffect(ssArgs, firstItem.getType());
            sLastrecord = firstItem.getType().toString();
        }
        seller.setVolatile("lastrecord", sLastrecord);
        return true;
    }
}

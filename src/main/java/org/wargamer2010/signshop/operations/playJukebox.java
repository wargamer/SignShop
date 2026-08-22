package org.wargamer2010.signshop.operations;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.data.Storage;
import org.wargamer2010.signshop.util.ItemMessagePart;
import org.wargamer2010.signshop.util.itemUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Shop operation that plays music discs from the shop chest in sequence.
 */
public class playJukebox implements SignShopOperation {
    private ItemStack[] getRecords(List<Block> containables) {
        List<ItemStack> tempItems = new ArrayList<>();
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
        isTotalItems = tempItems.toArray(new ItemStack[0]);
        return isTotalItems;
    }

    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(ssArgs.getContainables().isEmpty()) {
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("chest_missing", ssArgs.getMessageParts()));
            return false;
        }
        ItemStack[] isTotalItems = getRecords(ssArgs.getContainables().get());

        if(isTotalItems.length == 0) {
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("chest_empty", ssArgs.getMessageParts()));
            return false;
        }
        ssArgs.getItems().set(isTotalItems);
        ssArgs.setMessagePart("!items", ItemMessagePart.fromItems(ssArgs.getItems().get()));
        return true;
    }


    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        ItemStack[] isTotalItems = getRecords(ssArgs.getContainables().get());

        if(isTotalItems.length == 0) {
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("chest_empty", ssArgs.getMessageParts()));
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
        boolean doNext = false;
        int counter = 0;
        ItemStack firstItem = isTotalItems[0];
        for(ItemStack item : isTotalItems) {
            counter++;
            if (sLastrecord == null || doNext) {
                playEffect(ssArgs, item.getType());
                sLastrecord = item.getType().toString();
                if(sLastrecord == null)
                    doNext = true;
                break;
            }
            if(item.getType().toString().equals(sLastrecord))
                doNext = true;
            if (doNext && counter == isTotalItems.length) {
                playEffect(ssArgs, firstItem.getType());
                sLastrecord = firstItem.getType().toString();
                break;
            }
        }
        if (!doNext) {
            playEffect(ssArgs, firstItem.getType());
            sLastrecord = firstItem.getType().toString();
        }
        seller.setVolatile("lastrecord", sLastrecord);
        return true;
    }
}

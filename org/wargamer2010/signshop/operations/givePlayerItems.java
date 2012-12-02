package org.wargamer2010.signshop.operations;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.block.Block;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import java.util.List;
import java.util.ArrayList;

public class givePlayerItems implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(ssArgs.get_containables().isEmpty()) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("chest_missing", ssArgs.messageParts));
            return false;
        }
        List<ItemStack> tempItems = new ArrayList<ItemStack>();
        ItemStack[] isTotalItems = null;

        for(Block bHolder : ssArgs.get_containables()) {
            if(bHolder.getState() instanceof InventoryHolder) {
                InventoryHolder Holder = (InventoryHolder)bHolder.getState();
                for(ItemStack item : Holder.getInventory().getContents()) {
                    if(item != null && item.getAmount() > 0) {
                        tempItems.add(item);
                    }
                }
            }
        }
        isTotalItems = tempItems.toArray(new ItemStack[tempItems.size()]);

        if(isTotalItems.length == 0) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("chest_empty", ssArgs.messageParts));
            return false;
        }
        ssArgs.set_isItems(isTotalItems);
        ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.get_isItems()));
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(ssArgs.get_ssPlayer().getPlayer() == null)
            return true;
        if(ssArgs.isOperationParameter("oneslot")) {
            Boolean bEmptySlot = false;
            for(ItemStack stack : ssArgs.get_ssPlayer().getPlayer().getInventory().getContents()) {
                if(stack == null)
                    bEmptySlot = true;
            }
            if(!bEmptySlot) {
                ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("player_overstocked", ssArgs.messageParts));
                return false;
            }
        }
        if(!ssArgs.isOperationParameter("ignorefull")) {
            if(!itemUtil.isStockOK(ssArgs.get_ssPlayer().getPlayer().getInventory(), ssArgs.get_isItems(), false)) {
                ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("player_overstocked", ssArgs.messageParts));
                return false;
            }
        }
        ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.get_isItems()));
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ssArgs.get_ssPlayer().givePlayerItems(ssArgs.get_isItems());
        return true;
    }
}

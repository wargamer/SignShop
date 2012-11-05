package org.wargamer2010.signshop.operations;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.block.Block;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.util.itemUtil;
import java.util.List;
import java.util.LinkedList;

public class takePlayerItems implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(ssArgs.get_containables().isEmpty()) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("chest_missing", ssArgs.messageParts));
            return false;
        }
        List<ItemStack> tempItems = new LinkedList<ItemStack>();
        ItemStack[] isTotalItems;
        
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
        Player player = ssArgs.get_ssPlayer().getPlayer();
        ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.get_isItems()));
        if(!itemUtil.isStockOK(player.getInventory(), ssArgs.get_isItems(), true)) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("player_doesnt_have_items", ssArgs.messageParts));
            return false;
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ssArgs.get_ssPlayer().takePlayerItems(ssArgs.get_isItems());
        return true;
    }
}

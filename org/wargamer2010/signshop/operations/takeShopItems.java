package org.wargamer2010.signshop.operations;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.block.Block;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.util.itemUtil;
import java.util.List;
import java.util.LinkedList;

public class takeShopItems implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(ssArgs.get_containables().isEmpty()) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("chest_missing", ssArgs.getMessageParts()));
            return false;
        }
        ItemStack[] isTotalItems = itemUtil.getAllItemStacksForContainables(ssArgs.get_containables());

        if(isTotalItems.length == 0) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("chest_empty", ssArgs.getMessageParts()));
            return false;
        }
        ssArgs.set_isItems(isTotalItems);
        ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.get_isItems()));
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(ssArgs.get_isItems() == null) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("no_items_defined_for_shop", ssArgs.getMessageParts()));
            return false;
        }
        
        Boolean bStockOK = itemUtil.stockOKForContainables(ssArgs.get_containables(), ssArgs.get_isItems(), true);
        ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.get_isItems()));
        if(!bStockOK)
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("out_of_stock", ssArgs.getMessageParts()));
        if(!bStockOK && activeCheck)
            itemUtil.updateStockStatus(ssArgs.get_bSign(), ChatColor.DARK_RED);
        else if(activeCheck)
            itemUtil.updateStockStatus(ssArgs.get_bSign(), ChatColor.DARK_BLUE);

        return bStockOK;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        InventoryHolder Holder = itemUtil.getFirstStockOKForContainables(ssArgs.get_containables(), ssArgs.get_isItems(), true);
        if(Holder == null)
            return false;
        Holder.getInventory().removeItem(ssArgs.get_isItems());
        if(!itemUtil.stockOKForContainables(ssArgs.get_containables(), ssArgs.get_isItems(), true))
            itemUtil.updateStockStatus(ssArgs.get_bSign(), ChatColor.DARK_RED);
        else
            itemUtil.updateStockStatus(ssArgs.get_bSign(), ChatColor.DARK_BLUE);
        ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.get_isItems()));
        return true;
    }
}

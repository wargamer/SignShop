package org.wargamer2010.signshop.operations;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.block.Block;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.Material;

public class givePlayerItems implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(ssArgs.get_containables().isEmpty()) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("chest_missing", ssArgs.messageParts));
            return false;
        }
        ItemStack[] isTotalItems = itemUtil.getAllItemStacksForContainables(ssArgs.get_containables());

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
                if(stack == null || stack.getAmount() == 0 || stack.getType() == Material.AIR)
                    bEmptySlot = true;
            }
            if(!bEmptySlot) {
                ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("player_overstocked", ssArgs.messageParts));
                return false;
            }
        } else if(!ssArgs.isOperationParameter("ignorefull")) {
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

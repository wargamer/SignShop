package org.wargamer2010.signshop.operations;

import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.bukkit.Material;

public class givePlayerItems implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(ssArgs.getContainables().isEmpty()) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("chest_missing", ssArgs.getMessageParts()));
            return false;
        }
        ItemStack[] isTotalItems = itemUtil.getAllItemStacksForContainables(ssArgs.getContainables().get());

        if(!ssArgs.isOperationParameter("allowemptychest") && isTotalItems.length == 0) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("chest_empty", ssArgs.getMessageParts()));
            return false;
        }
        ssArgs.getItems().set(isTotalItems);
        ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.getItems().get()));
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(ssArgs.getPlayer().get().getPlayer() == null)
            return true;
        if(ssArgs.getItems().get() == null) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("no_items_defined_for_shop", ssArgs.getMessageParts()));
            return false;
        }

        ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.getItems().get()));
        if(ssArgs.isOperationParameter("oneslot")) {
            Boolean bEmptySlot = false;
            for(ItemStack stack : ssArgs.getPlayer().get().getPlayer().getInventory().getContents()) {
                if(stack == null || stack.getAmount() == 0 || stack.getType() == Material.AIR)
                    bEmptySlot = true;
            }
            if(!bEmptySlot) {
                if(ssArgs.isLeftClicking())
                    return false;
                ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("player_overstocked", ssArgs.getMessageParts()));
                return false;
            }
        } else if(!ssArgs.isOperationParameter("ignorefull")) {
            if(!itemUtil.isStockOK(ssArgs.getPlayer().get().getPlayer().getInventory(), ssArgs.getItems().get(), false)) {
                if(ssArgs.isLeftClicking())
                    return false;
                ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("player_overstocked", ssArgs.getMessageParts()));
                return false;
            }
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ssArgs.getPlayer().get().givePlayerItems(ssArgs.getItems().get());
        return true;
    }
}

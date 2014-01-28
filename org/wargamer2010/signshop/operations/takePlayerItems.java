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
        Player player = ssArgs.getPlayer().get().getPlayer();
        ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.getItems().get()));
        if(!itemUtil.isStockOK(player.getInventory(), ssArgs.getItems().get(), true)) {
            if(ssArgs.isLeftClicking())
                return false;
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("player_doesnt_have_items", ssArgs.getMessageParts()));
            return false;
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ssArgs.getPlayer().get().takePlayerItems(ssArgs.getItems().get());
        return true;
    }
}

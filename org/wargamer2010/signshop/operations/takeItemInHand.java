package org.wargamer2010.signshop.operations;

import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.util.itemUtil;

public class takeItemInHand implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(!ssArgs.isPlayerOnline())
            return true;
        if(ssArgs.getPlayer().get().getItemInHand() == null) {
            ssArgs.sendFailedRequirementsMessage("no_item_in_hand");
            return false;
        } else {
            ItemStack[] isItems = new ItemStack[1];
            isItems[0] = ssArgs.getPlayer().get().getItemInHand();
            ssArgs.getItems().set(isItems);
            ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.getItems().get()));
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ItemStack[] isItems = new ItemStack[1];
        isItems[0] = ssArgs.getPlayer().get().getItemInHand();
        ssArgs.getItems().set(isItems);
        ssArgs.getPlayer().get().takePlayerItems(ssArgs.getItems().get());
        return true;
    }
}

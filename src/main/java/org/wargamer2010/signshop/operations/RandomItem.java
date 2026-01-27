package org.wargamer2010.signshop.operations;

import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.ItemMessagePart;
import org.wargamer2010.signshop.util.itemUtil;

import java.util.Random;

/**
 * Shop operation that randomly selects one item from the shop chest to give.
 * Used for gambling-style shops like slot machines.
 */
public class RandomItem implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(ssArgs.getItems().get() == null) {
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("no_items_defined_for_shop", ssArgs.getMessageParts()));
            return false;
        }

        ssArgs.getItems().set(itemUtil.getMinimumAmount(ssArgs.getItems().get()));
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ItemStack isRandom = ssArgs.getItems().get()[(new Random()).nextInt(ssArgs.getItems().get().length)];
        ItemStack[] isRandoms = new ItemStack[1];
        isRandoms[0] = isRandom;
        ssArgs.getItems().set(isRandoms);
        ssArgs.setMessagePart("!items", ItemMessagePart.fromItems(ssArgs.getItems().get()));
        return true;
    }
}

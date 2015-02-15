package org.wargamer2010.signshop.operations;

import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.util.itemUtil;
import java.util.Random;
import org.wargamer2010.signshop.configuration.SignShopConfig;

public class RandomItem implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(ssArgs.getItems().get() == null) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("no_items_defined_for_shop", ssArgs.getMessageParts()));
            return false;
        }

        ssArgs.getItems().set(itemUtil.getMinimumAmount(ssArgs.getItems().get()));
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ItemStack isRandom = ssArgs.getItems().get()[(new Random()).nextInt(ssArgs.getItems().get().length)];
        ItemStack isRandoms[] = new ItemStack[1]; isRandoms[0] = isRandom;
        ssArgs.getItems().set(isRandoms);
        ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.getItems().get()));
        return true;
    }
}

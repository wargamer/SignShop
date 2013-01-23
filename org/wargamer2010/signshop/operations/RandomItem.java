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
        if(ssArgs.get_isItems() == null) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("no_items_defined_for_shop", null));
            return false;
        }

        ssArgs.set_isItems(itemUtil.getSingleAmount(ssArgs.get_isItems()));
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ItemStack isRandom = ssArgs.get_isItems()[(new Random()).nextInt(ssArgs.get_isItems().length)];
        ItemStack isRandoms[] = new ItemStack[1]; isRandoms[0] = isRandom;
        ssArgs.set_isItems(isRandoms);
        ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.get_isItems()));
        return true;
    }
}

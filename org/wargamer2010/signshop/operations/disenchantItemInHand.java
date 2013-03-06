package org.wargamer2010.signshop.operations;

import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import java.util.Map;

public class disenchantItemInHand implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        ItemStack isInHand = ssArgs.getPlayer().get().getItemInHand();
        if(isInHand == null) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("no_item_to_disenchant", ssArgs.getMessageParts()));
            return false;
        } else if(isInHand.getEnchantments().isEmpty()) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("nothing_to_disenchant", ssArgs.getMessageParts()));
            return false;
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ItemStack backup = ssArgs.getPlayer().get().getItemInHand();
        for(Map.Entry<Enchantment, Integer> entry : backup.getEnchantments().entrySet())
            backup.removeEnchantment(entry.getKey());
        return true;
    }
}

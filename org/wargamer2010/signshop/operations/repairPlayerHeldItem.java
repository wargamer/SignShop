package org.wargamer2010.signshop.operations;

import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.util.signshopUtil;

public class repairPlayerHeldItem implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        ItemStack isInHand = ssArgs.get_ssPlayer().getItemInHand();
        if(isInHand == null) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("no_item_to_repair", ssArgs.messageParts));
            return false;
        } else if(isInHand.getType().getMaxDurability() < 30) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("invalid_item_to_repair", ssArgs.messageParts));
            return false;
        } else if(isInHand.getEnchantments().size() > 0 && !SignShopConfig.getAllowEnchantedRepair() && !ssArgs.get_ssPlayer().hasPerm("SignShop.ignorerepair", false)) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("enchanted_not_allowed", ssArgs.messageParts));
            return false;
        } else if(isInHand.getDurability() == 0) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("item_already_repair", ssArgs.messageParts));
            return false;
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ssArgs.get_ssPlayer().getItemInHand().setDurability((short) 0);
        return true;
    }
}

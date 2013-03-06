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
        ItemStack isInHand = ssArgs.getPlayer().get().getItemInHand();
        if(isInHand == null) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("no_item_to_repair", ssArgs.getMessageParts()));
            return false;
        } else if(isInHand.getType().getMaxDurability() < 30) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("invalid_item_to_repair", ssArgs.getMessageParts()));
            return false;
        } else if(isInHand.getEnchantments().size() > 0 && !SignShopConfig.getAllowEnchantedRepair() && !ssArgs.getPlayer().get().hasPerm("SignShop.ignorerepair", false)) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("enchanted_not_allowed", ssArgs.getMessageParts()));
            return false;
        } else if(isInHand.getDurability() == 0) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("item_already_repair", ssArgs.getMessageParts()));
            return false;
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ssArgs.getPlayer().get().getItemInHand().setDurability((short) 0);
        return true;
    }
}

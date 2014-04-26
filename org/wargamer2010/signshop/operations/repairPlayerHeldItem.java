package org.wargamer2010.signshop.operations;

import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.util.signshopUtil;

public class repairPlayerHeldItem implements SignShopOperation {
    private void calculatePrice(SignShopArguments ssArgs) {
        ItemStack isInHand = ssArgs.getPlayer().get().getItemInHand();
        // Subtract the modifier from 1 because we want the repair to cost more when the durability on the item is lower (mod = 1 means the item is mint)
        // The item will never be mint and the price will never be completely 0 since we checked the damage level below
        if(ssArgs.isOperationParameter("variablecost"))
            ssArgs.getPrice().set(ssArgs.getPrice().get() * (1.0f - signshopUtil.calculateDurabilityModifier(new ItemStack[] { isInHand })));
    }

    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        ItemStack isInHand = ssArgs.getPlayer().get().getItemInHand();
        if(isInHand == null) {
            ssArgs.sendFailedRequirementsMessage("no_item_to_repair");
            return false;
        } else if(isInHand.getType().getMaxDurability() < 30) {
            ssArgs.sendFailedRequirementsMessage("invalid_item_to_repair");
            return false;
        } else if(isInHand.getEnchantments().size() > 0 && !SignShopConfig.getAllowEnchantedRepair() && !ssArgs.getPlayer().get().hasPerm("SignShop.ignorerepair", false)) {
            ssArgs.sendFailedRequirementsMessage("enchanted_not_allowed");
            return false;
        } else if(isInHand.getDurability() == 0) {
            ssArgs.sendFailedRequirementsMessage("item_already_repair");
            return false;
        }
        calculatePrice(ssArgs);
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        calculatePrice(ssArgs);
        ssArgs.getPlayer().get().getItemInHand().setDurability((short) 0);
        return true;
    }
}

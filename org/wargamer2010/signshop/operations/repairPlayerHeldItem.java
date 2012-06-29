package org.wargamer2010.signshop.operations;

import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.SignShop;

public class repairPlayerHeldItem implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        ItemStack isInHand = ssArgs.get_ssPlayer().getPlayer().getItemInHand();
        if(isInHand == null) {
            ssArgs.get_ssPlayer().sendMessage(SignShop.Errors.get("no_item_to_repair"));
            return false;
        } else if(isInHand.getType().getMaxDurability() < 30) {
            ssArgs.get_ssPlayer().sendMessage(SignShop.Errors.get("invalid_item_to_repair"));
            return false;
        } else if(isInHand.getEnchantments().size() > 0 && !SignShop.getAllowEnchantedRepair() && !ssArgs.get_ssPlayer().hasPerm("SignShop.ignorerepair", false)) {
            ssArgs.get_ssPlayer().sendMessage(SignShop.Errors.get("enchanted_not_allowed"));
            return false;
        } else if(isInHand.getDurability() == 0) {
            ssArgs.get_ssPlayer().sendMessage(SignShop.Errors.get("item_already_repair"));
            return false;
        }
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ssArgs.get_ssPlayer().getPlayer().getItemInHand().setDurability((short) 0);        
        return true;
    }
}

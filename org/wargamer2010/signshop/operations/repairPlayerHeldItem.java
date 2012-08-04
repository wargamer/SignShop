package org.wargamer2010.signshop.operations;

import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.signshopUtil;

public class repairPlayerHeldItem implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        ItemStack isInHand = ssArgs.get_ssPlayer().getPlayer().getItemInHand();
        if(isInHand == null) {
            ssArgs.get_ssPlayer().sendMessage(signshopUtil.getError("no_item_to_repair", ssArgs.messageParts));            
            return false;
        } else if(isInHand.getType().getMaxDurability() < 30) {
            ssArgs.get_ssPlayer().sendMessage(signshopUtil.getError("invalid_item_to_repair", ssArgs.messageParts));            
            return false;
        } else if(isInHand.getEnchantments().size() > 0 && !SignShop.getAllowEnchantedRepair() && !ssArgs.get_ssPlayer().hasPerm("SignShop.ignorerepair", false)) {
            ssArgs.get_ssPlayer().sendMessage(signshopUtil.getError("enchanted_not_allowed", ssArgs.messageParts));
            return false;
        } else if(isInHand.getDurability() == 0) {
            ssArgs.get_ssPlayer().sendMessage(signshopUtil.getError("item_already_repair", ssArgs.messageParts));            
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

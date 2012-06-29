package org.wargamer2010.signshop.operations;

import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.itemUtil;

public class takeItemInHand implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(ssArgs.get_ssPlayer().getPlayer() == null)
            return true;
        if(ssArgs.get_ssPlayer().getPlayer().getItemInHand() == null) {
            ssArgs.get_ssPlayer().sendMessage(SignShop.Errors.get("no_item_in_hand"));
            return false;
        } else {
            ItemStack[] isItems = new ItemStack[1];            
            isItems[0] = ssArgs.get_ssPlayer().getPlayer().getItemInHand();
            ssArgs.set_isItems(isItems);
            ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.get_isItems()));            
        }
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ItemStack[] isItems = new ItemStack[1];            
        isItems[0] = ssArgs.get_ssPlayer().getPlayer().getItemInHand();
        ssArgs.set_isItems(isItems);
        ssArgs.get_ssPlayer().takePlayerItems(ssArgs.get_isItems());
        return true;
    }
}

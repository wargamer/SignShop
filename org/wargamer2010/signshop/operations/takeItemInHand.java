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
        if(ssArgs.ssPlayer.getPlayer() == null)
            return true;
        if(ssArgs.ssPlayer.getPlayer().getItemInHand() == null || ssArgs.ssPlayer.getPlayer().getItemInHand().getType() == Material.AIR) {
            ssArgs.ssPlayer.sendMessage(SignShop.Errors.get("no_item_in_hand"));
            return false;
        } else {
            ssArgs.isItems = new ItemStack[1];
            ssArgs.isItems[0] = ssArgs.ssPlayer.getPlayer().getItemInHand();
            ssArgs.sItems = itemUtil.itemStackToString(ssArgs.isItems);
        }        
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ssArgs.isItems = new ItemStack[1];
        ssArgs.isItems[0] = ssArgs.ssPlayer.getPlayer().getItemInHand();
        ssArgs.ssPlayer.takePlayerItems(ssArgs.isItems);
        return true;
    }
}

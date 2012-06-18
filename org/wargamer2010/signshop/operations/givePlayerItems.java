package org.wargamer2010.signshop.operations;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.block.Block;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.itemUtil;
import java.util.List;
import java.util.ArrayList;

public class givePlayerItems implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(ssArgs.containables.isEmpty()) {
            ssArgs.ssPlayer.sendMessage(SignShop.Errors.get("chest_missing"));
            return false;
        }
        List<ItemStack> tempItems = new ArrayList<ItemStack>();
        ItemStack[] isTotalItems = null;
        
        for(Block bHolder : ssArgs.containables) {
            InventoryHolder Holder = (InventoryHolder)bHolder.getState();
            for(ItemStack item : Holder.getInventory().getContents()) {
                if(item != null && item.getAmount() > 0) {
                    tempItems.add(item);
                }
            }
        }
        isTotalItems = tempItems.toArray(new ItemStack[tempItems.size()]);

        if(isTotalItems.length == 0) {
            ssArgs.ssPlayer.sendMessage(SignShop.Errors.get("chest_empty"));
            return false;
        }
        ssArgs.isItems = isTotalItems;
        ssArgs.sItems = itemUtil.itemStackToString(isTotalItems);
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(ssArgs.ssPlayer.getPlayer() == null)
            return true;
        if(!itemUtil.isStockOK(ssArgs.ssPlayer.getPlayer().getInventory(), ssArgs.isItems, false)) {                        
            ssArgs.ssPlayer.sendMessage(SignShop.Errors.get("player_overstocked"));
            return false;
        }
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ItemStack[] isItems = (ssArgs.special.bActive && ssArgs.special.props.isItems != null ? ssArgs.special.props.isItems : ssArgs.isItems);
        ssArgs.ssPlayer.givePlayerItems(isItems);
        return true;
    }
}

package org.wargamer2010.signshop.operations;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.block.Block;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.itemUtil;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class giveShopItems implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(ssArgs.containables.isEmpty()) {
            ssArgs.ssPlayer.sendMessage(SignShop.Errors.get("chest_missing"));
            return false;
        }
        List<ItemStack> tempItems = new ArrayList<ItemStack>();
        ItemStack[] isTotalItems = null;
        
        for(Block bHolder : ssArgs.containables) {
            if(bHolder.getState() instanceof InventoryHolder) {
                InventoryHolder Holder = (InventoryHolder)bHolder.getState();
                for(ItemStack item : Holder.getInventory().getContents()) {
                    if(item != null && item.getAmount() > 0) {
                        tempItems.add(item);
                    }
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
        Boolean bStockOK = false;
        for(int i = 0; i < ssArgs.containables.size(); i++) {
            InventoryHolder Holder = (InventoryHolder)ssArgs.containables.get(i).getState();
            if(itemUtil.isStockOK(Holder.getInventory(), ssArgs.isItems, false))
                bStockOK = true;
        }
        if(!bStockOK)
            ssArgs.ssPlayer.sendMessage(SignShop.Errors.get("overstocked"));    
        if(activeCheck && !bStockOK)
            itemUtil.updateStockStatus(ssArgs.bSign, ChatColor.DARK_RED);
        else if(activeCheck)
            itemUtil.updateStockStatus(ssArgs.bSign, ChatColor.DARK_BLUE);
        return bStockOK;        
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        InventoryHolder Holder = null;
        Boolean bStockOK = false;
        for(int i = 0; i < ssArgs.containables.size(); i++) {            
            Holder = (InventoryHolder)ssArgs.containables.get(i).getState();
            if(itemUtil.isStockOK(Holder.getInventory(), ssArgs.isItems, false)) {
                bStockOK = true;
                break;
            }
        }        
        if(!bStockOK)
            return false;
        HashMap<Integer, ItemStack> isLeftOver = Holder.getInventory().addItem(ssArgs.isItems);                
        bStockOK = false;
        for(int i = 0; i < ssArgs.containables.size(); i++) {            
            Holder = (InventoryHolder)ssArgs.containables.get(i).getState();
            if(itemUtil.isStockOK(Holder.getInventory(), ssArgs.isItems, false)) {
                bStockOK = true;
                break;
            }
        }
        if(!bStockOK)
            itemUtil.updateStockStatus(ssArgs.bSign, ChatColor.DARK_RED);
        else
            itemUtil.updateStockStatus(ssArgs.bSign, ChatColor.DARK_BLUE);        
        return (isLeftOver.isEmpty());
    }
}

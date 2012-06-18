package org.wargamer2010.signshop.operations;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.block.Block;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.itemUtil;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class givePlayerRandomItem implements SignShopOperation {    
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
        InventoryHolder Holder = (InventoryHolder)ssArgs.containables.get(0).getState();
        if(!itemUtil.singeAmountStockOK(Holder.getInventory(), ssArgs.isItems, true)) {
            if(activeCheck)
                itemUtil.updateStockStatus(ssArgs.bSign, ChatColor.DARK_RED);
            ssArgs.ssPlayer.sendMessage(SignShop.Errors.get("out_of_stock"));
            return false;
        } else if(activeCheck)            
                itemUtil.updateStockStatus(ssArgs.bSign, ChatColor.DARK_BLUE);        
        return true;        
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {        
        InventoryHolder Holder = (InventoryHolder)ssArgs.containables.get(0).getState();
        ItemStack isRandom = ssArgs.isItems[(new Random()).nextInt(ssArgs.isItems.length)];
        ItemStack isRandoms[] = new ItemStack[1]; isRandoms[0] = isRandom;                
        Holder.getInventory().removeItem(isRandom);
        ssArgs.ssPlayer.givePlayerItems(isRandoms);                
        ssArgs.sItems = itemUtil.itemStackToString(isRandoms);
        if(!itemUtil.singeAmountStockOK(Holder.getInventory(), ssArgs.isItems, true))
            itemUtil.updateStockStatus(ssArgs.bSign, ChatColor.DARK_RED);
        else
            itemUtil.updateStockStatus(ssArgs.bSign, ChatColor.DARK_BLUE);
        ssArgs.ssPlayer.givePlayerItems(ssArgs.isItems);
        return true;
    }
}

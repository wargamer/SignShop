package org.wargamer2010.signshop.operations;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.block.Block;
import org.wargamer2010.signshop.util.signshopUtil;
import org.wargamer2010.signshop.util.itemUtil;
import java.util.List;
import java.util.LinkedList;

public class takeShopItems implements SignShopOperation {  
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(ssArgs.get_containables().isEmpty()) {
            ssArgs.get_ssPlayer().sendMessage(signshopUtil.getError("chest_missing", ssArgs.messageParts));            
            return false;
        }
        List<ItemStack> tempItems = new LinkedList<ItemStack>();
        ItemStack[] isTotalItems = null;        
        
        for(Block bHolder : ssArgs.get_containables()) {
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
            ssArgs.get_ssPlayer().sendMessage(signshopUtil.getError("chest_empty", ssArgs.messageParts));            
            return false;
        }
        ssArgs.set_isItems(isTotalItems);
        ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.get_isItems()));
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {           
        Boolean bStockOK = false;
        for(Block bHolder : ssArgs.get_containables()) {
            if(bHolder.getState() instanceof InventoryHolder) {
                InventoryHolder Holder = (InventoryHolder)bHolder.getState();            
                if(itemUtil.isStockOK(Holder.getInventory(), ssArgs.get_isItems(), true))
                    bStockOK = true;
            }
        }        
        ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.get_isItems()));
        if(!bStockOK)
            ssArgs.get_ssPlayer().sendMessage(signshopUtil.getError("out_of_stock", ssArgs.messageParts));            
        if(!bStockOK && activeCheck)
            itemUtil.updateStockStatus(ssArgs.get_bSign(), ChatColor.DARK_RED);
        else if(activeCheck)
            itemUtil.updateStockStatus(ssArgs.get_bSign(), ChatColor.DARK_BLUE);
        
        return bStockOK;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {        
        Boolean bStockOK = false;
        InventoryHolder Holder = null;
        
        for(Block bHolder : ssArgs.get_containables()) {
            if(bHolder.getState() instanceof InventoryHolder) {
                Holder = (InventoryHolder)bHolder.getState();            
                if(itemUtil.isStockOK(Holder.getInventory(), ssArgs.get_isItems(), true)) {
                    bStockOK = true;
                    break;              
                }
            }
        }
        if(!bStockOK)
            return false;
        Holder.getInventory().removeItem(ssArgs.get_isItems()); bStockOK = false;
        for(int i = 0; i< ssArgs.get_containables().size(); i++) {            
            Holder = (InventoryHolder)ssArgs.get_containables().get(i).getState();
            if(itemUtil.isStockOK(Holder.getInventory(), ssArgs.get_isItems(), true)) {
                bStockOK = true;
                break;
            }
        }
        if(!bStockOK)
            itemUtil.updateStockStatus(ssArgs.get_bSign(), ChatColor.DARK_RED);
        else
            itemUtil.updateStockStatus(ssArgs.get_bSign(), ChatColor.DARK_BLUE);
        ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.get_isItems()));
        return true;        
    }
}

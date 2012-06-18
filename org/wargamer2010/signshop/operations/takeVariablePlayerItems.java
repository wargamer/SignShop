package org.wargamer2010.signshop.operations;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.block.Block;
import org.bukkit.ChatColor;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.itemUtil;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class takeVariablePlayerItems implements SignShopOperation {    
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
        Player player = ssArgs.ssPlayer.getPlayer();
        InventoryHolder Holder = (InventoryHolder)ssArgs.containables.get(0).getState();        
        HashMap<ItemStack[], Float> variableAmount = itemUtil.variableAmount(player.getInventory(), Holder.getInventory(), ssArgs.isItems, false);        
        Float iCount = (Float)variableAmount.values().toArray()[0];
        if(iCount == 0.0f) {
            ssArgs.ssPlayer.sendMessage(SignShop.Errors.get("player_doesnt_have_items").replace("!items", ssArgs.sItems));
            return false;
        } else if(iCount == -1.0f) {
            if(activeCheck)
                itemUtil.updateStockStatus(ssArgs.bSign, ChatColor.DARK_RED);
            ssArgs.ssPlayer.sendMessage(SignShop.Errors.get("overstocked"));
            return false;
        } else if(activeCheck) {
            itemUtil.updateStockStatus(ssArgs.bSign, ChatColor.DARK_BLUE);
        }
        ItemStack[] isActual = (ItemStack[])variableAmount.keySet().toArray()[0];
        ssArgs.isItems = isActual;
        ssArgs.sItems = itemUtil.itemStackToString(isActual);
        ssArgs.fPrice = (ssArgs.fPrice * iCount);
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        InventoryHolder Holder = (InventoryHolder)ssArgs.containables.get(0).getState();
        itemUtil.variableAmount(ssArgs.ssPlayer.getPlayer().getInventory(), Holder.getInventory(), ssArgs.isItems, true);
        if(!itemUtil.isStockOK(Holder.getInventory(), ssArgs.isItems, false))
            itemUtil.updateStockStatus(ssArgs.bSign, ChatColor.DARK_RED);
        else
            itemUtil.updateStockStatus(ssArgs.bSign, ChatColor.DARK_BLUE);
        return true;
    }
}

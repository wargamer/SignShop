package org.wargamer2010.signshop.operations;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.block.Block;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

public class takeVariablePlayerItems implements SignShopOperation {    
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
        if(ssArgs.get_ssPlayer().getPlayer() == null)
            return true;        
        Player player = ssArgs.get_ssPlayer().getPlayer();
        ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.get_isItems()));
        HashMap<ItemStack[], Float> variableAmount = itemUtil.variableAmount(player.getInventory(), ssArgs.get_isItems(), false);        
        Float iCount = (Float)variableAmount.values().toArray()[0];
        if(iCount == 0.0f) {
            ssArgs.get_ssPlayer().sendMessage(signshopUtil.getError("player_doesnt_have_items", ssArgs.messageParts));
            return false;        
        }        
        
        ItemStack[] isActual = (ItemStack[])variableAmount.keySet().toArray()[0];
        ssArgs.set_isItems(isActual);
        ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.get_isItems()));
        ssArgs.set_fPrice(ssArgs.get_fPrice() * iCount);        
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        HashMap<ItemStack[], Float> variableAmount = itemUtil.variableAmount(ssArgs.get_ssPlayer().getPlayer().getInventory(), ssArgs.get_isItems(), true);                
        Float iCount = (Float)variableAmount.values().toArray()[0];
        ItemStack[] isActual = (ItemStack[])variableAmount.keySet().toArray()[0];
        ssArgs.set_isItems(isActual);
        ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.get_isItems()));
        ssArgs.set_fPrice(ssArgs.get_fPrice() * iCount);        
        return true;
    }
}

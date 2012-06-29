package org.wargamer2010.signshop.operations;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.block.Block;
import org.bukkit.ChatColor;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.itemUtil;
import java.util.List;
import java.util.LinkedList;
import org.wargamer2010.signshop.util.signshopUtil;

public class takePlayerItems implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(ssArgs.get_containables().isEmpty()) {
            ssArgs.get_ssPlayer().sendMessage(SignShop.Errors.get("chest_missing"));
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
            ssArgs.get_ssPlayer().sendMessage(SignShop.Errors.get("chest_empty"));
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
        if(!itemUtil.isStockOK(player.getInventory(), ssArgs.get_isItems(), true)) {
            ssArgs.get_ssPlayer().sendMessage(signshopUtil.getError("player_doesnt_have_items", ssArgs.messageParts));
            return false;
        }        
        return true;                
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {        
        InventoryHolder Holder = (InventoryHolder)ssArgs.get_containables().get(0).getState();
        itemUtil.variableAmount(ssArgs.get_ssPlayer().getPlayer().getInventory(), Holder.getInventory(), ssArgs.get_isItems(), true);
        if(!itemUtil.isStockOK(Holder.getInventory(), ssArgs.get_isItems(), false))
            itemUtil.updateStockStatus(ssArgs.get_bSign(), ChatColor.DARK_RED);
        else
            itemUtil.updateStockStatus(ssArgs.get_bSign(), ChatColor.DARK_BLUE);
        return true;
    }
}

package org.wargamer2010.signshop.operations;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Effect;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.itemUtil;

public class playJukebox implements SignShopOperation {    
    private ItemStack[] getRecords(List<Block> containables) {
        List<ItemStack> tempItems = new ArrayList<ItemStack>();
        ItemStack[] isTotalItems = null;
        
        for(Block bHolder : containables) {
            if(bHolder.getState() instanceof InventoryHolder) {
                InventoryHolder Holder = (InventoryHolder)bHolder.getState();
                for(ItemStack item : Holder.getInventory().getContents()) {
                    if(item != null && item.getAmount() > 0 && itemUtil.isDisc(item.getTypeId())) {
                        tempItems.add(item);
                    }
                }
            }             
        }
        isTotalItems = tempItems.toArray(new ItemStack[tempItems.size()]);
        return isTotalItems;
    }
    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {        
        if(ssArgs.get_containables().isEmpty()) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("chest_missing", ssArgs.messageParts));
            return false;
        }        
        ItemStack[] isTotalItems = getRecords(ssArgs.get_containables());
        
        if(isTotalItems.length == 0) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("chest_empty", ssArgs.messageParts));            
            return false;
        }
        ssArgs.set_isItems(isTotalItems);        
        ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.get_isItems()));
        return true;
    }
       
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {        
        ItemStack[] isTotalItems = getRecords(ssArgs.get_containables());
        
        if(isTotalItems.length == 0) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("chest_empty", ssArgs.messageParts));            
            return false;
        }
        
        return true;
    }
    
    private void playEffect(SignShopArguments ssArgs, int id) {
        ssArgs.get_bSign().getWorld().playEffect(ssArgs.get_bSign().getLocation(), Effect.RECORD_PLAY, id);
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ItemStack[] isTotalItems = getRecords(ssArgs.get_containables());
        Seller seller = SignShop.Storage.getSeller(ssArgs.get_bSign().getLocation());
        String sLastrecord = seller.getVolatile("lastrecord");
        Integer iLastrecord = -1;
        if(sLastrecord != null)
            iLastrecord = Integer.parseInt(sLastrecord);
        Boolean doNext = false;
        Integer counter = 0;
        ItemStack firstItem = isTotalItems[0];
        for(ItemStack item : isTotalItems) {
            counter++;
            if(iLastrecord == -1 || doNext == true) {
                playEffect(ssArgs, item.getTypeId());                
                iLastrecord = item.getTypeId();                
                if(iLastrecord == -1)
                    doNext = true;
                break;
            }
            if(item.getTypeId() == iLastrecord)
                doNext = true;
            if(doNext == true && counter == isTotalItems.length) {
                playEffect(ssArgs, firstItem.getTypeId());
                iLastrecord = firstItem.getTypeId();
                break;
            }
        }
        if(doNext == false) {
            playEffect(ssArgs, firstItem.getTypeId());
            iLastrecord = firstItem.getTypeId();
        }
        seller.setVolatile("lastrecord", Integer.toString(iLastrecord));
        return true;
    }
}

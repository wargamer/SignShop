package org.wargamer2010.signshop.operations;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.wargamer2010.signshop.SignShop;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

public class enchantItemInHand implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(ssArgs.containables.isEmpty()) {
            ssArgs.ssPlayer.sendMessage(SignShop.Errors.get("chest_missing"));
            return false;
        }
        
        Map<Enchantment, Integer> AllEnchantments = new HashMap<Enchantment, Integer>();
        Map<Enchantment, Integer> TempEnchantments = new HashMap<Enchantment, Integer>();
        
        for(Block bHolder : ssArgs.containables) {
            InventoryHolder Holder = (InventoryHolder)bHolder.getState();
            for(ItemStack item : Holder.getInventory().getContents()) {
                if(item != null && item.getAmount() > 0) {
                    TempEnchantments = item.getEnchantments();
                    if(TempEnchantments.isEmpty()) continue;
                    for(Map.Entry<Enchantment, Integer> enchantment : TempEnchantments.entrySet()) {
                        if(AllEnchantments.containsKey(enchantment.getKey()) && AllEnchantments.get(enchantment.getKey()) > enchantment.getValue())
                            TempEnchantments.remove(enchantment.getKey());
                    }
                    if(!TempEnchantments.isEmpty())
                        AllEnchantments.putAll(TempEnchantments);
                }
            }
        }
        if(AllEnchantments.isEmpty()) {
            ssArgs.ssPlayer.sendMessage(SignShop.Errors.get("enchantment_missing"));
            return false;
        }
        ssArgs.miscSettings.put("enchantmentInHand", signshopUtil.convertEnchantmentsToString(AllEnchantments));
        ssArgs.sEnchantments = itemUtil.enchantmentsToMessageFormat(AllEnchantments);
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(ssArgs.ssPlayer.getPlayer() == null)
            return true;
        if(!ssArgs.miscSettings.containsKey("enchantmentInHand")) {
            SignShop.log(("misc property enchantmentInHand was not found for shop @ " + signshopUtil.convertLocationToString(ssArgs.bSign.getLocation())), Level.WARNING);
            return false;
        }
        Map<Enchantment, Integer> enchantments = signshopUtil.convertStringToEnchantments(ssArgs.miscSettings.get("enchantmentInHand"));
        ssArgs.sEnchantments = itemUtil.enchantmentsToMessageFormat(enchantments);
        ItemStack isInHand = ssArgs.ssPlayer.getPlayer().getItemInHand();        
        ItemStack isBackup = new ItemStack(
            isInHand.getType(),
            isInHand.getAmount(),
            isInHand.getDurability()
        );
        if(isInHand.getData() != null){
            isBackup.setData(isInHand.getData());
        }
        if(isInHand == null) {
            ssArgs.ssPlayer.sendMessage(SignShop.Errors.get("item_not_enchantable"));
            return false;
        } else if(!itemUtil.addSafeEnchantments(isBackup, enchantments)) {
            ssArgs.ssPlayer.sendMessage(SignShop.Errors.get("item_not_enchantable"));
            return false;
        }        
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ItemStack isInHand = ssArgs.ssPlayer.getPlayer().getItemInHand();
        Map<Enchantment, Integer> enchantments = signshopUtil.convertStringToEnchantments(ssArgs.miscSettings.get("enchantmentInHand"));
        return itemUtil.addSafeEnchantments(isInHand, enchantments);        
    }
}

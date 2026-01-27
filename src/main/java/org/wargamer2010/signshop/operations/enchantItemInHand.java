package org.wargamer2010.signshop.operations;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Shop operation that applies enchantments from enchanted books in shop chest to the player's held item.
 */
public class enchantItemInHand implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(ssArgs.getContainables().isEmpty()) {
            if(ssArgs.isOperationParameter("allowNoChests"))
                return true;
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("chest_missing", ssArgs.getMessageParts()));
            return false;
        }

        Map<Enchantment, Integer> AllEnchantments = new HashMap<>();
        Map<Enchantment, Integer> TempEnchantments;

        for(Block bHolder : ssArgs.getContainables().get()) {
            if(bHolder.getState() instanceof InventoryHolder) {
                InventoryHolder Holder = (InventoryHolder)bHolder.getState();
                for(ItemStack item : Holder.getInventory().getContents()) {
                    if(item != null && item.getAmount() > 0) {
                        if(item.getType() == Material.ENCHANTED_BOOK && item.hasItemMeta() && item.getItemMeta() instanceof EnchantmentStorageMeta) {
                            TempEnchantments = ((EnchantmentStorageMeta)item.getItemMeta()).getStoredEnchants();
                        } else {
                            TempEnchantments = item.getEnchantments();
                        }
                        
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
        }
        if(AllEnchantments.isEmpty()) {
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("enchantment_missing", ssArgs.getMessageParts()));
            return false;
        }
        ssArgs.miscSettings.put("enchantmentInHand", signshopUtil.convertEnchantmentsToString(AllEnchantments));
        ssArgs.setMessagePart("!enchantments", itemUtil.enchantmentsToMessageFormat(AllEnchantments));
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(ssArgs.getPlayer().get().getPlayer() == null)
            return true;
        if(!ssArgs.miscSettings.containsKey("enchantmentInHand")) {
            SignShop.log(("misc property enchantmentInHand was not found for shop @ " + signshopUtil.convertLocationToString(ssArgs.getSign().get().getLocation())), Level.WARNING);
            return false;
        }
        Map<Enchantment, Integer> enchantments = signshopUtil.convertStringToEnchantments(ssArgs.miscSettings.get("enchantmentInHand"));
        String enchantmentsString = itemUtil.enchantmentsToMessageFormat(enchantments);
        ssArgs.setMessagePart("!enchantments", enchantmentsString);
        ItemStack isInHand = ssArgs.getPlayer().get().getItemInHand();
        ItemStack isBackup = itemUtil.getBackupSingleItemStack(isInHand);
        if(isInHand == null) {
            ssArgs.sendFailedRequirementsMessage("item_not_enchantable");
            return false;
        } else if(!itemUtil.needsEnchantment(isBackup, enchantments)) {
            ssArgs.sendFailedRequirementsMessage("item_already_enchanted");
            return false;
        }
        
        if(!itemUtil.safelyAddEnchantments(isBackup, enchantments)) {
            ssArgs.sendFailedRequirementsMessage("item_not_enchantable");
            return false;
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ItemStack isInHand = ssArgs.getPlayer().get().getItemInHand();
        Map<Enchantment, Integer> enchantments = signshopUtil.convertStringToEnchantments(ssArgs.miscSettings.get("enchantmentInHand"));
        
        if(isInHand.getAmount() > 1) {
            ItemStack single = itemUtil.getBackupSingleItemStack(isInHand);
            single.setAmount(1);
            
            ItemStack[] singleStacks = new ItemStack[] { single };
            Map<Integer, ItemStack> taken = ssArgs.getPlayer().get().takePlayerItems(singleStacks);
            if(!taken.isEmpty()) {
                ssArgs.sendFailedRequirementsMessage("no_item_in_hand");
                return false;
            }
            
            boolean didEnchantment = itemUtil.safelyAddEnchantments(single, enchantments);
            if(!didEnchantment)
                return false;
            
            Map<Integer, ItemStack> given = ssArgs.getPlayer().get().givePlayerItems(singleStacks);
            if(!given.isEmpty()) {
                ItemStack singleOriginal = itemUtil.getBackupSingleItemStack(isInHand);
                singleOriginal.setAmount(1);
                given = ssArgs.getPlayer().get().givePlayerItems(new ItemStack[] { singleOriginal });
                
                ssArgs.sendFailedRequirementsMessage("player_overstocked");
                
                if(!given.isEmpty()) {
                    String message = "Failed enchantment of item in hand because inventory is too full for player."
                                        + " Player: " + ssArgs.getPlayer().get().getName() 
                                        + ", Shop: (" + ssArgs.getSign().get().getX() + ", " + ssArgs.getSign().get().getY() + ")";
                    
                    SignShop.log(message, Level.WARNING);
                }

                return false;
            }
            
            return true;
        } else {
            return itemUtil.safelyAddEnchantments(isInHand, enchantments);
        }
    }
}

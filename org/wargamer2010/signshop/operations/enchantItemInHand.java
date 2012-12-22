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
import org.wargamer2010.signshop.configuration.SignShopConfig;

public class enchantItemInHand implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(ssArgs.get_containables().isEmpty()) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("chest_missing", ssArgs.messageParts));
            return false;
        }

        Map<Enchantment, Integer> AllEnchantments = new HashMap<Enchantment, Integer>();
        Map<Enchantment, Integer> TempEnchantments = new HashMap<Enchantment, Integer>();

        for(Block bHolder : ssArgs.get_containables()) {
            if(bHolder.getState() instanceof InventoryHolder) {
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
        }
        if(AllEnchantments.isEmpty()) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("enchantment_missing", ssArgs.messageParts));
            return false;
        }
        ssArgs.miscSettings.put("enchantmentInHand", signshopUtil.convertEnchantmentsToString(AllEnchantments));
        ssArgs.setMessagePart("!enchantments", itemUtil.enchantmentsToMessageFormat(AllEnchantments));
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(ssArgs.get_ssPlayer().getPlayer() == null)
            return true;
        if(!ssArgs.miscSettings.containsKey("enchantmentInHand")) {
            SignShop.log(("misc property enchantmentInHand was not found for shop @ " + signshopUtil.convertLocationToString(ssArgs.get_bSign().getLocation())), Level.WARNING);
            return false;
        }
        Map<Enchantment, Integer> enchantments = signshopUtil.convertStringToEnchantments(ssArgs.miscSettings.get("enchantmentInHand"));
        String enchantmentsString = itemUtil.enchantmentsToMessageFormat(enchantments);
        ssArgs.setMessagePart("!enchantments", enchantmentsString);
        ItemStack isInHand = ssArgs.get_ssPlayer().getPlayer().getItemInHand();
        ItemStack isBackup = itemUtil.getBackupSingleItemStack(isInHand);
        if(isInHand == null) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("item_not_enchantable", ssArgs.messageParts));
            return false;
        } else if(!itemUtil.safelyAddEnchantments(isBackup, enchantments)) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("item_not_enchantable", ssArgs.messageParts));
            return false;
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ItemStack isInHand = ssArgs.get_ssPlayer().getPlayer().getItemInHand();
        Map<Enchantment, Integer> enchantments = signshopUtil.convertStringToEnchantments(ssArgs.miscSettings.get("enchantmentInHand"));
        return itemUtil.safelyAddEnchantments(isInHand, enchantments);
    }
}

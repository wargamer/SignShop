package org.wargamer2010.signshop.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.material.MaterialData;
import org.bukkit.Material;
import org.bukkit.material.SimpleAttachableMaterialData;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.operations.SignShopOperation;
import org.wargamer2010.signshop.operations.SignShopArguments;

public class itemUtil {
    private static HashMap<Integer, String> discs;
    
    public static void initDiscs() {
        // This is pretty ugly but I really couldn't find another way in
        // bukkit's source to get this via a native function
        // Source: http://www.minecraftwiki.net/wiki/Data_values
        discs = new HashMap<Integer, String>();
        discs.put(2256, "13 Disc");
        discs.put(2257, "Cat Disc");
        discs.put(2258, "Blocks Disc");
        discs.put(2259, "Chirp Disc");
        discs.put(2260, "Far Disc");
        discs.put(2261, "Mall Disc");
        discs.put(2262, "Mellohi Disc");
        discs.put(2263, "Stal Disc");
        discs.put(2264, "Strad Disc");
        discs.put(2265, "Ward Disc");
        discs.put(2266, "11 Disc");
    }
    
    public static ItemStack[] getSingleAmount(ItemStack[] isItems) {
        List<ItemStack> items = new ArrayList<ItemStack>();
        for(ItemStack item: isItems) {
            ItemStack isBackup = new CraftItemStack(
                item.getType(),
                1,
                item.getDurability()
            );
            addSafeEnchantments(isBackup, item.getEnchantments());
            if(item.getData() != null) {
                isBackup.setData(item.getData());
            }
            if(!items.contains(isBackup))
                items.add(isBackup);
        }
        ItemStack[] isBackupToTake = new ItemStack[items.size()];
        int i = 0;
        for(ItemStack entry : items) {
            isBackupToTake[i] = entry;
            i++;
        }
        return isBackupToTake;
    }
    
    public static Boolean singeAmountStockOK(Inventory iiInventory, ItemStack[] isItemsToTake, Boolean bTakeOrGive) {                
        return isStockOK(iiInventory, getSingleAmount(isItemsToTake), bTakeOrGive);
    }
    
    public static Boolean isStockOK(Inventory iiInventory, ItemStack[] isItemsToTake, Boolean bTakeOrGive) {
        try {
            ItemStack[] isChestItems = iiInventory.getContents();
            ItemStack[] isBackup = getBackupItemStack(isChestItems);
            ItemStack[] isBackupToTake = getBackupItemStack(isItemsToTake);                
            HashMap<Integer, ItemStack> leftOver;
            if(bTakeOrGive)
                leftOver = iiInventory.removeItem(isBackupToTake);
            else
                leftOver = iiInventory.addItem(isBackupToTake);
            Boolean bStockOK = true;
            if(!leftOver.isEmpty())
                bStockOK = false;
            iiInventory.setContents(isBackup);        
            return bStockOK;
        } catch(NullPointerException ex) {            
            // Chest is not available, contents are NULL. So let's assume the Stock is not OK
            return false;
        }
    }
    
    private static String binaryToRoman(int binary) {
        final String[] RCODE = {"M", "CM", "D", "CD", "C", "XC", "L",
                                           "XL", "X", "IX", "V", "IV", "I"};
        final int[]    BVAL  = {1000, 900, 500, 400,  100,   90,  50,
                                               40,   10,    9,   5,   4,    1};
        if (binary <= 0 || binary >= 4000) {
            return "";
        }
        String roman = "";
        for (int i = 0; i < RCODE.length; i++) {
            while (binary >= BVAL[i]) {
                binary -= BVAL[i];
                roman  += RCODE[i];
            }
        }
        return roman;
    }  
    
    private static String lookupDisc(int id) {        
        if(discs.containsKey(id))
            return discs.get(id);
        else
            return "";
    }
    
    public static String formatData(MaterialData data) {
        // For some reason running tostring on data when it's from an attachable material
        // will cause a NullPointerException, thus if we're dealing with an attachable, go the easy way :)
        if(data instanceof SimpleAttachableMaterialData)
            return stringFormat(data.getItemType().name());
        String sData;
        if(!(sData = lookupDisc(data.getItemTypeId())).equals(""))            
            return sData;
        else
            sData = data.toString().toLowerCase();
        Pattern p = Pattern.compile("\\(-?[0-9]+\\)");
        Matcher m = p.matcher(sData);
        sData = m.replaceAll("");
        sData = sData.replace("_", " ");
        
        StringBuffer sb = new StringBuffer(sData.length());
        p = Pattern.compile("(^|\\W)([a-z])");
        m = p.matcher(sData);
        while(m.find()) {
            m.appendReplacement(sb, m.group(1) + m.group(2).toUpperCase() );
        }
        
        m.appendTail(sb);
        
        return sb.toString();
    }

    private static String stringFormat(String sMaterial){
        sMaterial = sMaterial.replace("_"," ");
        Pattern p = Pattern.compile("(^|\\W)([a-z])");
        Matcher m = p.matcher(sMaterial.toLowerCase());
        StringBuffer sb = new StringBuffer(sMaterial.length());

        while(m.find()){
            m.appendReplacement(sb, m.group(1) + m.group(2).toUpperCase() );
        }

        m.appendTail(sb);

        return sb.toString();
    }
        
    public static String itemStackToString(ItemStack[] isStacks) {
        HashMap<ItemStack, Integer> items = new HashMap<ItemStack, Integer>();
        HashMap<ItemStack, Map<Enchantment,Integer>> enchantments = new HashMap<ItemStack, Map<Enchantment,Integer>>();
        String sItems = "";
        Boolean first = true;        
        Integer tempAmount = 0;
        for(ItemStack item: isStacks) {
            if(item == null)
                continue;
            ItemStack isBackup = new CraftItemStack(
                item.getType(),
                1,
                item.getDurability()
            );
            addSafeEnchantments(isBackup, item.getEnchantments());
            if(item.getData() != null){
                isBackup.setData(item.getData());
            }
            
            if(item.getEnchantments().size() > 0)
                enchantments.put(isBackup, item.getEnchantments());
            if(items.containsKey(isBackup)) {
                tempAmount = (items.get(isBackup) + item.getAmount());                
                items.put(isBackup, tempAmount);
            } else 
                items.put(isBackup, item.getAmount());
        }
        for(Map.Entry<ItemStack, Integer> entry : items.entrySet()) {
            if(first) first = false;
            else sItems += ", ";
            if(enchantments.containsKey(entry.getKey()))
                sItems += ChatColor.DARK_PURPLE;
            String sDamaged = " ";
            if(entry.getKey().getType().getMaxDurability() >= 30 && entry.getKey().getDurability() != 0)
                sDamaged = " Damaged ";
            sItems += (entry.getValue()) + sDamaged + formatData(entry.getKey().getData());
            if(enchantments.containsKey(entry.getKey())) {                
                sItems += (ChatColor.WHITE + " " + enchantmentsToMessageFormat(enchantments.get(entry.getKey())));
            }
        }
        
        return sItems;
    }
    
    public static String enchantmentsToMessageFormat(Map<Enchantment,Integer> enchantments) {
        String enchantmentMessage = "";
        Boolean eFirst = true;
        
        enchantmentMessage += "(";
        for(Map.Entry<Enchantment,Integer> eEntry : enchantments.entrySet()) {
            if(eFirst) eFirst = false;
            else enchantmentMessage += ", ";
            enchantmentMessage += (stringFormat(eEntry.getKey().getName()) + " " + binaryToRoman(eEntry.getValue()));
        }
        enchantmentMessage += ")";
        return enchantmentMessage;
    }
        
    public static void setSignStatus(Block sign, ChatColor color) {
        if(clickedSign(sign)) {
            Sign signblock = ((Sign) sign.getState());
            String[] sLines = signblock.getLines();            
            if(ChatColor.stripColor(sLines[0]).length() < 14) {
                signblock.setLine(0, (color + ChatColor.stripColor(sLines[0])));
                signblock.update();
            }
        }
    }
    
    public static Boolean addSafeEnchantments(ItemStack isEnchantMe, Map<Enchantment, Integer> enchantments) {        
        if(enchantments.isEmpty())
            return true;
        try {
            isEnchantMe.addEnchantments(enchantments);
        } catch(IllegalArgumentException ex) {
            if(SignShopConfig.getAllowUnsafeEnchantments()) {
                try {
                    isEnchantMe.addUnsafeEnchantments(enchantments);                    
                } catch(IllegalArgumentException exfinal) {
                    return false;                    
                }
            } else
                return false;
        }
        return true;
    }    
    
    public static HashMap<ItemStack, Integer> StackToMap(ItemStack[] isStacks) {
        ItemStack[] isBackup = getBackupItemStack(isStacks);
        HashMap<ItemStack, Integer> mReturn = new HashMap<ItemStack, Integer>();
        int tempAmount = 0;
        for(int i = 0; i < isBackup.length; i++) {
            if(isBackup[i] == null) continue;
            tempAmount = isBackup[i].getAmount();
            isBackup[i].setAmount(1);
            if(mReturn.containsKey(isBackup[i])) {
                tempAmount += mReturn.get(isBackup[i]);
                mReturn.remove(isBackup[i]);
                mReturn.put(isBackup[i], tempAmount);
            } else 
                mReturn.put(isBackup[i], tempAmount);
        }
        return mReturn;
    }
    
    public static ItemStack[] getBackupItemStack(ItemStack[] isOriginal) {
        ItemStack[] isBackup = new ItemStack[isOriginal.length];        
        for(int i = 0; i < isOriginal.length; i++){
            if(isOriginal[i] != null) {
                isBackup[i] = getBackupSingleItemStack(isOriginal[i]);
            }
        }
        return isBackup;
    }
    
    public static ItemStack getBackupSingleItemStack(ItemStack isOriginal) {
        ItemStack isBackup = new CraftItemStack(
            isOriginal.getType(),
            isOriginal.getAmount(),
            isOriginal.getDurability()
        );
        itemUtil.addSafeEnchantments(isBackup, isOriginal.getEnchantments());
        if(isOriginal.getType() == Material.WRITTEN_BOOK || isOriginal.getType() == Material.BOOK_AND_QUILL) {
            itemTags.copyTags(isOriginal, isBackup);
        }

        if(isOriginal.getData() != null) {
            isBackup.setData(isOriginal.getData());
        }
        
        return isBackup;
    }
    
    public static HashMap<ItemStack[], Float> variableAmount(Inventory iiFrom, ItemStack[] isItemsToTake, Boolean bTake) {
        ItemStack[] isBackup = getBackupItemStack(isItemsToTake);
        HashMap<ItemStack[], Float> returnMap = new HashMap<ItemStack[], Float>();
        returnMap.put(isItemsToTake, 1.0f);
        Boolean fromOK = itemUtil.isStockOK(iiFrom, isBackup, true);        
        if(fromOK) {            
            returnMap.put(isItemsToTake, 1.0f);
            if(bTake)
                iiFrom.removeItem(isBackup);            
            return returnMap;
        } else if(!SignShopConfig.getAllowVariableAmounts() && !fromOK) {            
            returnMap.put(isItemsToTake, 0.0f);
            return returnMap;
        }
        returnMap.put(isItemsToTake, 0.0f);
        float iCount = 0;
        float tempCount = 0;
        int i = 0;        
        HashMap<ItemStack, Integer> mItemsToTake = StackToMap(isBackup);        
        HashMap<ItemStack, Integer> mInventory = StackToMap(iiFrom.getContents());        
        ItemStack[] isActual = new ItemStack[mItemsToTake.size()];
        for(Map.Entry<ItemStack, Integer> entry : mItemsToTake.entrySet()) {            
            if(iCount == 0 && mInventory.containsKey(entry.getKey()))
                iCount = ((float)mInventory.get(entry.getKey()) / (float)entry.getValue());                
            else if(iCount != 0 && mInventory.containsKey(entry.getKey())) {
                tempCount = ((float)mInventory.get(entry.getKey()) / (float)entry.getValue());                
                if(tempCount != iCount)
                    return returnMap;
            } else
                return returnMap;
            
            isActual[i] = new CraftItemStack(
                entry.getKey().getType(),
                mInventory.get(entry.getKey()),
                entry.getKey().getDurability()
            );
            addSafeEnchantments(isActual[i], entry.getKey().getEnchantments());            
            if(entry.getKey().getType() == Material.WRITTEN_BOOK || entry.getKey().getType() == Material.BOOK_AND_QUILL) {
                itemTags.copyTags(entry.getKey(), isActual[i]);
            }
            if(entry.getKey().getData() != null) {
                isActual[i].setData(entry.getKey().getData());
            }
            i++;
        }
        returnMap.clear();        
        returnMap.put(isActual, iCount);
        if(bTake)
            iiFrom.removeItem(isActual);        
        return returnMap;
    }
    
    public static void updateStockStatusPerChest(Block bHolder, Block bIgnore) {
        List<Block> signs = SignShop.Storage.getSignsFromHolder(bHolder);        
        if(signs != null) {
            for (Block temp : signs) {
                if(temp == bIgnore)
                    continue;
                Seller seller = null;
                if(!clickedSign(temp))                    
                    continue;
                if((seller = SignShop.Storage.getSeller(temp.getLocation())) != null) {                                                
                    String[] sLines = ((Sign) temp.getState()).getLines();
                    if(!SignShopConfig.Operations.containsKey(signshopUtil.getOperation(sLines[0])))
                        continue;                    
                    List operation = SignShopConfig.Operations.get(signshopUtil.getOperation(sLines[0]));
                    Map<SignShopOperation, List> SignShopOperations = signshopUtil.getSignShopOps(operation);
                    if(SignShopOperations == null)
                        return;
                    SignShopArguments ssArgs = new SignShopArguments(economyUtil.parsePrice(sLines[3]), seller.getItems(), seller.getContainables(), seller.getActivatables(), 
                                                                        null, null, temp, signshopUtil.getOperation(sLines[0]), null);
                    if(seller.getMisc() != null)
                        ssArgs.miscSettings = seller.getMisc();
                    Boolean reqOK = true;
                    for(Map.Entry<SignShopOperation, List> ssOperation : SignShopOperations.entrySet()) {
                        ssArgs.operationParameters = ssOperation.getValue();
                        if(!(reqOK = ssOperation.getKey().checkRequirements(ssArgs, false))) {                            
                            itemUtil.setSignStatus(temp, ChatColor.DARK_RED);                            
                            break;
                        }
                    }
                    if(reqOK)
                        itemUtil.setSignStatus(temp, ChatColor.DARK_BLUE);                            
                }
            }
        }
    }
    
    public static void updateStockStatus(Block bSign, ChatColor ccColor) {
        Seller seTemp;
        if((seTemp = SignShop.Storage.getSeller(bSign.getLocation())) != null) {
            List<Block> iChests = seTemp.getContainables();
            for(Block bHolder : iChests)
                updateStockStatusPerChest(bHolder, bSign);            
        }
        setSignStatus(bSign, ccColor);
    }
    
    public static Boolean clickedSign(Block bBlock) {
        if(bBlock.getType() == Material.SIGN || bBlock.getType() == Material.WALL_SIGN || bBlock.getType() == Material.SIGN_POST)
            return true;
        else
            return false;
    }
    
    public static ItemStack[] convertStringtoItemStacks(List<String> sItems) {
        ItemStack isItems[] = new ItemStack[sItems.size()];
        for(int i = 0; i < sItems.size(); i++) {
            try {
                String[] sItemprops = sItems.get(i).split(Storage.itemSeperator);
                if(sItemprops.length < 4)
                    continue;
                isItems[i] = new CraftItemStack(                        
                        Integer.parseInt(sItemprops[1]),
                        Integer.parseInt(sItemprops[0]),
                        Short.parseShort(sItemprops[2])
                );
                isItems[i].getData().setData(new Byte(sItemprops[3]));
                if(sItemprops.length > 4)
                    isItems[i].addEnchantments(signshopUtil.convertStringToEnchantments(sItemprops[4]));
            } catch(Exception ex) {                              
                continue;
            }
        }        
        return isItems;
    }
    
    public static String[] convertItemStacksToString(ItemStack[] isItems) {        
        List<String> sItems = new ArrayList();
                
        ItemStack isCurrent = null;
        for(int i = 0; i < isItems.length; i++) {
            if(isItems[i] != null) {
                isCurrent = isItems[i];
                sItems.add((isCurrent.getAmount() + SignShop.Storage.itemSeperator 
                        + isCurrent.getTypeId() + SignShop.Storage.itemSeperator  
                        + isCurrent.getDurability() + SignShop.Storage.itemSeperator  
                        + isCurrent.getData().getData() + SignShop.Storage.itemSeperator 
                        + signshopUtil.convertEnchantmentsToString(isCurrent.getEnchantments())));
            }
            
        }
        String[] items = new String[sItems.size()];
        sItems.toArray(items);
        return items;
    }
}

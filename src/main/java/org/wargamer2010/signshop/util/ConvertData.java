package org.wargamer2010.signshop.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.blocks.*;
import org.wargamer2010.signshop.configuration.Storage;

import java.util.ArrayList;
import java.util.List;

public class ConvertData { //TODO will use this to convert the old '&' delimitited data to BukkitSerialization

    public static ItemStack[] convertOldStringtoItemStacks(List<String> itemStringList) {
        IItemTags itemTags = BookFactory.getItemTags();
        ItemStack[] itemStacks = new ItemStack[itemStringList.size()];
        int invalidItems = 0;

        for (int i = 0; i < itemStringList.size(); i++) {
            try {
                String[] itemProperties = itemStringList.get(i).split(Storage.getItemSeperator());
                if (itemProperties.length < 4) {
                    invalidItems++;
                    continue;
                }

                if (itemProperties.length <= 7) {
                    if (i < (itemStringList.size() - 1) && itemStringList.get(i + 1).split(Storage.getItemSeperator()).length < 4) {
                        // Bug detected, the next item will be the base64 string belonging to the current item
                        // This bug will be fixed at the next save as the ~ will be replaced with a |
                        itemProperties = (itemStringList.get(i) + "|" + itemStringList.get(i + 1)).split(Storage.getItemSeperator());
                    }
                }

                if (itemProperties.length > 7) {
                    String base64prop = itemProperties[7];
                    // The ~ and | are used to differentiate between the old NBTLib and the BukkitSerialization
                    if (base64prop != null && (base64prop.startsWith("~") || base64prop.startsWith("|"))) {
                        String joined = itemUtil.Join(itemProperties, 7).substring(1);

                        ItemStack[] convertedStacks = BukkitSerialization.itemStackArrayFromBase64(joined);
                        if (convertedStacks.length > 0 && convertedStacks[0] != null) {
                            itemStacks[i] = convertedStacks[0];
                        }
                    }
                }

                if (itemStacks[i] == null) {
                    itemStacks[i] = itemTags.getCraftItemstack(
                            Material.getMaterial(itemProperties[1]),
                            Integer.parseInt(itemProperties[0]),
                            Short.parseShort(itemProperties[2])
                    );
                    itemStacks[i].getData().setData(new Byte(itemProperties[3]));

                    if (itemProperties.length > 4)
                        itemUtil.safelyAddEnchantments(itemStacks[i], signshopUtil.convertStringToEnchantments(itemProperties[4]));
                }

                if (itemProperties.length > 5) {
                    try {
                        itemStacks[i] = SignShopBooks.addBooksProps(itemStacks[i], Integer.parseInt(itemProperties[5]));
                    } catch (NumberFormatException ignored) {

                    }
                }
                if (itemProperties.length > 6) {
                    try {
                        SignShopItemMeta.setMetaForID(itemStacks[i], Integer.parseInt(itemProperties[6]));
                    } catch (NumberFormatException ignored) {

                    }
                }
            } catch (Exception ignored) {

            }
        }

        if (invalidItems > 0) {
            ItemStack[] temp = new ItemStack[itemStringList.size() - invalidItems];
            int counter = 0;
            for (ItemStack i : itemStacks) {
                if (i != null) {
                    temp[counter] = i;
                    counter++;
                }
            }

            itemStacks = temp;
        }


        return itemStacks;
    }

    public static String[] convertItemStacksToOldString(ItemStack[] itemStackArray) {//TODO May not need this but saving anyway
        List<String> itemStringList = new ArrayList<>();
        if (itemStackArray == null)
            return new String[1];

        ItemStack currentItemStack;
        for (ItemStack itemStack : itemStackArray) {
            if (itemStack != null) {
                currentItemStack = itemStack;
                String ID = "";
                if (itemUtil.isWriteableBook(currentItemStack))
                    ID = SignShopBooks.getBookID(currentItemStack).toString();
                String metaID = SignShopItemMeta.getMetaID(currentItemStack).toString();
                if (metaID.equals("-1"))
                    metaID = "";
                ItemStack[] stacks = new ItemStack[1];
                stacks[0] = currentItemStack;

                itemStringList.add(BukkitSerialization.itemStackArrayToBase64(stacks));

                itemStringList.add((currentItemStack.getAmount() + Storage.getItemSeperator()
                        + currentItemStack.getType() + Storage.getItemSeperator()
                        + currentItemStack.getDurability() + Storage.getItemSeperator()
                        + currentItemStack.getData().getData() + Storage.getItemSeperator()
                        + signshopUtil.convertEnchantmentsToString(currentItemStack.getEnchantments()) + Storage.getItemSeperator()
                        + ID + Storage.getItemSeperator()
                        + metaID + Storage.getItemSeperator()
                        + "|" + BukkitSerialization.itemStackArrayToBase64(stacks)));
            }

        }
        String[] items = new String[itemStringList.size()];
        itemStringList.toArray(items);
        return items;
    }
}

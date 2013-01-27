package org.wargamer2010.signshop.operations;

import java.util.Comparator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.bukkit.Material;

public class takeVariablePlayerItems implements SignShopOperation {
    static <K,V extends Comparable<? super V>> void SortEntriesByValue(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
            new Comparator<Map.Entry<K,V>>() {
                @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                    int res = e1.getValue().compareTo(e2.getValue());
                    return res != 0 ? res : 1;
                }
            }
        );
        sortedEntries.addAll(map.entrySet());
        map.clear();
        for(Map.Entry<K,V> entry : sortedEntries) {
            map.put(entry.getKey(), entry.getValue());
        }
    }

    private boolean doDurabilityNullification(SignShopArguments ssArgs) {
        ItemStack[] inv_stacks = ssArgs.get_ssPlayer().getInventoryContents();
        if(ssArgs.isOperationParameter("acceptdamaged")) {
            short nodamage = 0;
            Material mat;
            Map<ItemStack, Integer> map = itemUtil.StackToMap(ssArgs.get_isItems());
            if(map.size() > 1) {
                ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("damaged_items_shop_homogeneous", ssArgs.messageParts));
                return false;
            }
            ItemStack arr[] = new ItemStack[1]; map.keySet().toArray(arr);
            mat = arr[0].getType();

            for(ItemStack stack : inv_stacks) {
                if(stack != null && stack.getType() == mat && stack.getType().getMaxDurability() >= 30 && stack.getDurability() != nodamage) {
                    stack.setDurability(nodamage);
                }
            }
            return true;
        }
        return false;
    }

    private ItemStack[] getRealItemStack(ItemStack[] playerinv, ItemStack[] actual) {
        Map<ItemStack,Short> sortedbydurability = new LinkedHashMap<ItemStack, Short>();
        for(ItemStack playerstack : playerinv) {
            if(playerstack != null)
                sortedbydurability.put(playerstack, playerstack.getDurability());
        }
        SortEntriesByValue(sortedbydurability);

        Map<ItemStack, Integer> map = itemUtil.StackToMap(actual);
        ItemStack neededstack = null;
        int needed = -1;
        for(Map.Entry<ItemStack, Integer> entry : map.entrySet()) {
            neededstack = entry.getKey();
            needed = entry.getValue();
        }

        List<ItemStack> toTakeForReal = new LinkedList<ItemStack>();

        for(ItemStack stackfrominv : sortedbydurability.keySet()) {
            if(itemUtil.itemstackEqual(stackfrominv, neededstack, true)) {
                ItemStack bak = itemUtil.getBackupSingleItemStack(stackfrominv);
                if(bak.getAmount() >= needed)
                    bak.setAmount(needed);
                toTakeForReal.add(bak);
                needed -= bak.getAmount();
                if(needed <= 0)
                    break;
            }
        }

        ItemStack[] arr = new ItemStack[toTakeForReal.size()];
        return toTakeForReal.toArray(arr);
    }

    private float calculateDurabilityModifier(ItemStack[] stacks) {
        if(stacks.length == 0)
            return 1.0f;
        float totalmod = 0.0f;
        float totalamount = 0;
        for(ItemStack stack : stacks) {
            float dur = stack.getDurability();
            float max = stack.getType().getMaxDurability();
            float amount = stack.getAmount();
            totalmod += ((dur/max) * amount);
            totalamount += amount;
        }
        return (1.0f - (totalmod / totalamount));
    }

    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(ssArgs.get_containables().isEmpty()) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("chest_missing", ssArgs.messageParts));
            return false;
        }
        ItemStack[] isTotalItems = itemUtil.getAllItemStacksForContainables(ssArgs.get_containables());

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
        if(ssArgs.get_ssPlayer().getPlayer() == null)
            return true;
        if(ssArgs.get_isItems() == null) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("no_items_defined_for_shop", null));
            return false;
        }

        ItemStack[] backupinv = itemUtil.getBackupItemStack(ssArgs.get_ssPlayer().getInventoryContents());
        boolean didnull = doDurabilityNullification(ssArgs);

        Player player = ssArgs.get_ssPlayer().getPlayer();
        ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.get_isItems()));
        HashMap<ItemStack[], Float> variableAmount = itemUtil.variableAmount(player.getInventory(), ssArgs.get_isItems());
        Float iCount = (Float)variableAmount.values().toArray()[0];

        ssArgs.get_ssPlayer().setInventoryContents(backupinv);
        if(iCount == 0.0f) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("player_doesnt_have_items", ssArgs.messageParts));
            return false;
        }

        ItemStack[] isActual = (ItemStack[])variableAmount.keySet().toArray()[0];
        float pricemod = 1.0f;
        if(didnull) {
            ItemStack[] temp = getRealItemStack(backupinv, isActual);
            if(temp.length > 0) {
                isActual = temp;
                pricemod = calculateDurabilityModifier(isActual);
            }
        }

        ssArgs.set_isItems(isActual);
        ssArgs.setMessagePart("!items", itemUtil.itemStackToString(ssArgs.get_isItems()));
        ssArgs.set_fPrice(ssArgs.get_fPrice() * iCount * pricemod);
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        if(!checkRequirements(ssArgs, true))
            return false;
        ssArgs.get_ssPlayer().takePlayerItems(ssArgs.get_isItems());
        return true;
    }
}

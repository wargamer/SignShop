
package org.wargamer2010.signshop.player;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.util.itemUtil;
import static org.wargamer2010.signshop.util.itemUtil.StackToMap;
import static org.wargamer2010.signshop.util.itemUtil.getBackupItemStack;

/**
 * Wraps a real inventory and allows for "virtual" operations to be executed which means it doesn't actually touch the inventory
 */
public class VirtualInventory {
    private final Inventory inventory;

    public VirtualInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    /**
     * Attempts to take all of the items defined by isItemsToTake or a simple part (1/2 of all request items, 1/4, etc.)
     * @param isItemsToTake Items to take as a whole or an equal part of each individual stack
     * @return A map filled with the items that can be taken which may be the same as specified by isItemsToTake or a part. Returns an empty map in case of failure.
     */
    public HashMap<ItemStack[], Double> variableAmount(ItemStack[] isItemsToTake) {
        ItemStack[] isBackup = getBackupItemStack(isItemsToTake);
        HashMap<ItemStack[], Double> returnMap = new HashMap<ItemStack[], Double>();
        returnMap.put(isItemsToTake, 1.0d);
        Boolean fromOK = hasItems(isBackup);

        if(fromOK) {
            returnMap.put(isItemsToTake, 1.0d);
            return returnMap;
        } else if(!SignShopConfig.getAllowVariableAmounts() && !fromOK) {
            returnMap.put(isItemsToTake, 0.0d);
            return returnMap;
        }
        returnMap.put(isItemsToTake, 0.0d);
        double iCount = 0;
        double tempCount;
        int i = 0;
        HashMap<ItemStack, Integer> mItemsToTake = StackToMap(isBackup);
        HashMap<ItemStack, Integer> mInventory = StackToMap(inventory.getContents());
        ItemStack[] isActual = new ItemStack[mItemsToTake.size()];
        for(Map.Entry<ItemStack, Integer> entry : mItemsToTake.entrySet()) {
            if(iCount == 0 && mInventory.containsKey(entry.getKey()))
                iCount = ((double)mInventory.get(entry.getKey()) / (double)entry.getValue());
            else if(iCount != 0 && mInventory.containsKey(entry.getKey())) {
                tempCount = ((double)mInventory.get(entry.getKey()) / (double)entry.getValue());
                if(tempCount != iCount)
                    return returnMap;
            } else
                return returnMap;

            isActual[i] = itemUtil.getBackupSingleItemStack(entry.getKey());
            isActual[i].setAmount(mInventory.get(entry.getKey()));

            i++;
        }
        returnMap.clear();
        returnMap.put(isActual, iCount);
        return returnMap;
    }

    /**
     * Checks whether either the given isItemsToTake can be taken from or added to the inventory as a whole
     * @param isItemsToTake Items to attempt to add or take
     * @param bTakeOrGive True to check whether the items can be taken
     * @return True if there is either sufficient space or the required items available
     */
    public boolean isStockOK(ItemStack[] isItemsToTake, boolean bTakeOrGive) {
        try {
            if(bTakeOrGive)
                return hasItems(isItemsToTake);
            else
                return canTakeItems(isItemsToTake);
        } catch(NullPointerException ex) {
            // Chest is not available, contents are NULL. So let's assume the Stock is not OK
            return false;
        }
    }

    /**
     * Checks whether the given isItemsToTake can be taken from the inventory as a whole
     * @param isItemsToTake Items to attempt to take
     * @return True if the required items are available
     */
    public boolean hasItems(ItemStack[] isItemsToTake) {
        ItemStack[] isBackup = getBackupItemStack(isItemsToTake);

        HashMap<ItemStack, Integer> mItemsToTake = StackToMap(isBackup);
        HashMap<ItemStack, Integer> mInventory = StackToMap(inventory.getContents());

        for(Map.Entry<ItemStack, Integer> entry : mItemsToTake.entrySet()) {
            if(mInventory.containsKey(entry.getKey())) {
                if(mInventory.get(entry.getKey()) < entry.getValue()) {
                    return false;
                }
            } else {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks whether the given isItemsToGive can be added to the inventory as a whole
     * @param isItemsToGive Items to attempt to add to the inventory
     * @return True if there is sufficient space
     */
    public boolean canTakeItems(ItemStack[] isItemsToGive) {
        Map<Integer, StackWithAmount> lookaside = getAmountsMapping();
        for (int i = 0; i < isItemsToGive.length; i++) {
            ItemStack item = isItemsToGive[i];
            int amountToAdd = item.getAmount();

            while (amountToAdd > 0) {
                int partialSpaceIndex = findSpace(item, lookaside, false);

                if (partialSpaceIndex == -1) {
                    int freeSpaceIndex = findSpace(item, lookaside, true);

                    if (freeSpaceIndex == -1) {
                        return false;
                    } else {
                        int toSet = amountToAdd;

                        if (amountToAdd > inventory.getMaxStackSize()) {
                            amountToAdd -= inventory.getMaxStackSize();
                            toSet = inventory.getMaxStackSize();
                        } else {
                            amountToAdd = 0;
                        }

                        lookaside.put(freeSpaceIndex, new StackWithAmount(toSet, item));
                    }
                } else {
                    StackWithAmount stackWithAmount = lookaside.get(partialSpaceIndex);
                    int partialAmount = stackWithAmount.getAmount();
                    int maxAmount = stackWithAmount.getStack().getMaxStackSize();

                    int toSet;
                    if (amountToAdd + partialAmount <= maxAmount) {
                        toSet = amountToAdd + partialAmount;
                        amountToAdd = 0;
                    } else {
                        toSet = maxAmount;
                        amountToAdd = amountToAdd + partialAmount - maxAmount;
                    }

                    lookaside.get(partialSpaceIndex).setAmount(toSet);
                }
            }
        }

        return true;
    }

    private int findSpace(ItemStack item, Map<Integer, StackWithAmount> lookaside, boolean findEmpty) {
        ItemStack[] stacks = inventory.getContents();
        if (item == null) {
            return -1;
        }

        for (int i = 0; i < stacks.length; i++) {
            boolean contains = lookaside.containsKey(i);
            if(findEmpty && !contains) {
                return i;
            } else if(!findEmpty && contains) {
                StackWithAmount compareWith = lookaside.get(i);
                if(compareWith != null) {
                    ItemStack compareWithStack = compareWith.getStack();
                    if (compareWith.getAmount() < compareWithStack.getMaxStackSize() && compareWithStack.isSimilar(item)) {
                        return i;
                    }
                }
            }
        }

        return -1;
    }

    private Map<Integer, StackWithAmount> getAmountsMapping() {
        Map<Integer, StackWithAmount> map = new LinkedHashMap<Integer, StackWithAmount>();
        ItemStack[] stacks = inventory.getContents();

        for(int i = 0; i < stacks.length; i++) {
            ItemStack stack = stacks[i];
            if(stack != null)
                map.put(i, new StackWithAmount(stack.getAmount(), stack));
        }

        return map;
    }

    private class StackWithAmount {
        private int amount;
        private ItemStack stack;

        private StackWithAmount(int amount, ItemStack stack) {
            this.amount = amount;
            this.stack = stack;
        }

        private int getAmount() {
            return amount;
        }

        private void setAmount(int amount) {
            this.amount = amount;
        }

        private ItemStack getStack() {
            return stack;
        }

        private void setStack(ItemStack stack) {
            this.stack = stack;
        }
    }
}

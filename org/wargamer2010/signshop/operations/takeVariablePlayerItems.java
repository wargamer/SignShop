package org.wargamer2010.signshop.operations;

import java.util.Collections;
import java.util.Comparator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.bukkit.Material;

public class takeVariablePlayerItems implements SignShopOperation {
    private static class StackDurabilityPair implements Comparator<StackDurabilityPair> {
        private ItemStack _stack;
        private Short _durability;

        private StackDurabilityPair(ItemStack stack, Short durability) {
            _stack = stack;
            _durability = durability;
        }

        private StackDurabilityPair() {

        }

        public ItemStack getStack() {
            return _stack;
        }

        public Short getDurability() {
            return _durability;
        }

        @Override
        public int compare(StackDurabilityPair o1, StackDurabilityPair o2) {
            if (!(o1 instanceof StackDurabilityPair) || !(o2 instanceof StackDurabilityPair))
                throw new ClassCastException();

            return (o1.getDurability() - o2.getDurability());
        }
    }

    private boolean doDurabilityNullification(SignShopArguments ssArgs) {
        ItemStack[] inv_stacks = ssArgs.get_ssPlayer().getInventoryContents();
        if(ssArgs.isOperationParameter("acceptdamaged")) {
            short nodamage = 0;
            Material mat;
            Map<ItemStack, Integer> map = itemUtil.StackToMap(ssArgs.get_isItems());
            if(map.size() > 1) {
                ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("damaged_items_shop_homogeneous", ssArgs.getMessageParts()));
                return false;
            }
            ItemStack arr[] = new ItemStack[1]; map.keySet().toArray(arr);
            mat = arr[0].getType();
            boolean didnull = false;

            for(ItemStack stack : inv_stacks) {
                if(stack != null && stack.getType() == mat && stack.getType().getMaxDurability() >= 30 && stack.getDurability() != nodamage) {
                    stack.setDurability(nodamage);
                    didnull = true;
                }
            }
            return didnull;
        }
        return false;
    }

    private ItemStack[] getRealItemStack(ItemStack[] playerinv, ItemStack[] actual) {
        List<StackDurabilityPair> sortedbydurability = new LinkedList<StackDurabilityPair>();
        for(ItemStack playerstack : playerinv) {
            if(playerstack != null)
                sortedbydurability.add(new StackDurabilityPair(playerstack, playerstack.getDurability()));
        }
        Collections.sort(sortedbydurability, new StackDurabilityPair());

        Map<ItemStack, Integer> map = itemUtil.StackToMap(actual);
        ItemStack neededstack;
        int needed;
        List<ItemStack> toTakeForReal = new LinkedList<ItemStack>();

        for(Map.Entry<ItemStack, Integer> entry : map.entrySet()) {
            neededstack = entry.getKey();
            needed = entry.getValue();
            for(StackDurabilityPair pair : sortedbydurability) {
                ItemStack stackfrominv = pair.getStack();
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
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("chest_missing", ssArgs.getMessageParts()));
            return false;
        }
        ItemStack[] isTotalItems = itemUtil.getAllItemStacksForContainables(ssArgs.get_containables());

        if(isTotalItems.length == 0) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("chest_empty", ssArgs.getMessageParts()));
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
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("no_items_defined_for_shop", ssArgs.getMessageParts()));
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
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("player_doesnt_have_items", ssArgs.getMessageParts()));
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

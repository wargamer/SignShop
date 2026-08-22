package org.wargamer2010.signshop.operations;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.ItemMessagePart;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.*;

/**
 * Shop operation that takes variable amounts of items from player based on inventory contents, with durability adjustments.
 */
public class takeVariablePlayerItems implements SignShopOperation {

    private boolean doDurabilityNullification(SignShopArguments ssArgs) {
        ItemStack[] inv_stacks = ssArgs.getPlayer().get().getInventoryContents();
        if(ssArgs.isOperationParameter("acceptdamaged")) {
            short nodamage = 0;
            Material mat;
            Map<ItemStack, Integer> map = itemUtil.StackToMap(ssArgs.getItems().get());
            if(map.size() > 1) {
                ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("damaged_items_shop_homogeneous", ssArgs.getMessageParts()));
                return false;
            }
            ItemStack[] arr = new ItemStack[1];
            map.keySet().toArray(arr);
            mat = arr[0].getType();
            boolean didnull = false;

            for(ItemStack stack : inv_stacks) {
                if(stack != null && stack.getType() == mat && stack.getType().getMaxDurability() >= 30 && ((Damageable) stack.getItemMeta()).getDamage() != nodamage) {
                    Damageable meta = (Damageable) stack.getItemMeta();
                    meta.setDamage(nodamage);
                    stack.setItemMeta(meta);
                    didnull = true;
                }
            }
            return didnull;
        }
        return false;
    }

    private ItemStack[] getRealItemStack(ItemStack[] playerinv, ItemStack[] actual) {
        List<StackDurabilityPair> sortedbydurability = new LinkedList<>();
        for(ItemStack playerstack : playerinv) {
            if(playerstack != null)
                sortedbydurability.add(new StackDurabilityPair(playerstack, (short) ((Damageable) playerstack).getDamage()));
        }
        sortedbydurability.sort(new StackDurabilityPair());

        Map<ItemStack, Integer> map = itemUtil.StackToMap(actual);
        ItemStack neededstack;
        int needed;
        List<ItemStack> toTakeForReal = new LinkedList<>();

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

    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(ssArgs.getContainables().isEmpty()) {
            if(ssArgs.isOperationParameter("allowNoChests"))
                return true;
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("chest_missing", ssArgs.getMessageParts()));
            return false;
        }
        ItemStack[] isTotalItems = itemUtil.getAllItemStacksForContainables(ssArgs.getContainables().get());

        if(isTotalItems.length == 0) {
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("chest_empty", ssArgs.getMessageParts()));
            return false;
        }
        ssArgs.getItems().set(isTotalItems);
        ssArgs.setMessagePart("!items", ItemMessagePart.fromItems(ssArgs.getItems().get()));
        return true;
    }

    /**
     * Validates player has required items for variable-amount transactions.
     * Runs before every variable shop operation.
     */
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(!ssArgs.isPlayerOnline())
            return true;
        if(ssArgs.getItems().get() == null) {
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("no_items_defined_for_shop", ssArgs.getMessageParts()));
            return false;
        }

        ItemStack[] backupinv = itemUtil.getBackupItemStack(ssArgs.getPlayer().get().getInventoryContents());
        boolean didnull = doDurabilityNullification(ssArgs);

        SignShopPlayer ssPlayer = ssArgs.getPlayer().get();

        ssArgs.setMessagePart("!items", ItemMessagePart.fromItems(ssArgs.getItems().get()));
        HashMap<ItemStack[], Double> variableAmount = ssPlayer.getVirtualInventory().variableAmount(ssArgs.getItems().get());
        Double iCount = (Double)variableAmount.values().toArray()[0];

        ssArgs.getPlayer().get().setInventoryContents(backupinv);

        ItemStack[] isActual = (ItemStack[])variableAmount.keySet().toArray()[0];
        double pricemod = 1.0d;
        if(didnull) {
            ItemStack[] temp = getRealItemStack(backupinv, isActual);
            if(temp.length > 0) {
                isActual = temp;
                pricemod = signshopUtil.calculateDurabilityModifier(isActual);
            }
        }

        ssArgs.getItems().set(isActual);
        ssArgs.setMessagePart("!items", ItemMessagePart.fromItems(ssArgs.getItems().get()));
        if(iCount != 0.0d)
            ssArgs.getPrice().set(ssArgs.getPrice().get() * iCount * pricemod);
        else
            ssArgs.getPrice().set(ssArgs.getPrice().get() * pricemod);

        // Cache calculated items and final price in messageParts (transient, survives reset())
        // No serialization needed - messageParts stores Objects directly
        if(iCount != 0.0d && isActual != null && isActual.length > 0) {
            ssArgs.setMessagePart("_takeVarItems_cached", isActual);
            ssArgs.setMessagePart("_takeVarItems_finalPrice", ssArgs.getPrice().get());
        }

        if(iCount == 0.0d) {
            ssArgs.sendFailedRequirementsMessage("player_doesnt_have_items");
            return false;
        }
        return true;
    }

    /**
     * Executes variable-amount item transfer from player to shop.
     * Core operation executed on every variable shop use.
     */
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        // Use cached calculations if available (avoids duplicate work after reset)
        if(ssArgs.hasMessagePart("_takeVarItems_cached")) {
            ItemStack[] cachedItems = (ItemStack[]) ssArgs.getRawMessageParts().get("_takeVarItems_cached");
            Double cachedPrice = (Double) ssArgs.getRawMessageParts().get("_takeVarItems_finalPrice");

            if(cachedItems != null && cachedItems.length > 0 && cachedPrice != null) {
                ssArgs.getItems().set(cachedItems);
                ssArgs.setMessagePart("!items", ItemMessagePart.fromItems(cachedItems));

                // Restore the final calculated price (reset() cleared it back to root)
                ssArgs.getPrice().set(cachedPrice);

                // Execute transfer
                boolean transactedAll = ssArgs.getPlayer().get()
                    .takePlayerItems(ssArgs.getItems().get()).isEmpty();
                if(!transactedAll)
                    ssArgs.getPlayer().get().sendMessage(
                        SignShop.getInstance().getSignShopConfig().getError(
                            "could_not_complete_operation", null));
                return transactedAll;
            }
        }

        // FALLBACK: Cache miss - recalculate (safety net)
        if(!checkRequirements(ssArgs, true))
            return false;
        boolean transactedAll = ssArgs.getPlayer().get().takePlayerItems(ssArgs.getItems().get()).isEmpty();
        if(!transactedAll)
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("could_not_complete_operation", null));
        return transactedAll;
    }

    private static class StackDurabilityPair implements Comparator<StackDurabilityPair> {
        private ItemStack stack;
        private Short durability;

        private StackDurabilityPair(ItemStack stack, Short durability) {
            this.stack = stack;
            this.durability = durability;
        }

        private StackDurabilityPair() {

        }

        public ItemStack getStack() {
            return stack;
        }

        public Short getDurability() {
            return durability;
        }

        @Override
        public int compare(StackDurabilityPair o1, StackDurabilityPair o2) {
            if (!(o1 instanceof StackDurabilityPair) || !(o2 instanceof StackDurabilityPair))
                throw new ClassCastException();

            return (o1.getDurability() - o2.getDurability());
        }
    }
}

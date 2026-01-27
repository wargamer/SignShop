package org.wargamer2010.signshop.operations;

import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.ItemMessagePart;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import static org.wargamer2010.signshop.operations.SignShopArguments.separator;

/**
 * Operation that selects a specific numbered chest for multi-chest shop configurations.
 * Allows Trade shops to use separate chests for different item sets (Chest{1}, Chest{2}).
 */
public class Chest implements SignShopOperation {
    private Boolean incorrectPar(SignShopArguments ssArgs) {
        ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("could_not_complete_operation", null));
        SignShop.log("Invalid Chest{}, check your config.yml!", Level.WARNING);
        return false;
    }

    private Block checkChestAmount(SignShopArguments ssArgs, Integer iChestnumber) {
        Block bHolder = null;
        int iCount = 0;
        for(Block bTemp : ssArgs.getContainables().getRoot()) {
            if(bTemp.getState() instanceof InventoryHolder) {
                iCount++;
                if(iCount == iChestnumber)
                    bHolder = bTemp;
            }
        }
        return bHolder;
    }

    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(!ssArgs.hasOperationParameters())
            return incorrectPar(ssArgs);
        int iChestnumber;
        try {
            iChestnumber = Integer.parseInt(ssArgs.getFirstOperationParameter());
        } catch(NumberFormatException ex) {
            return incorrectPar(ssArgs);
        }
        if(iChestnumber < 1)
            return incorrectPar(ssArgs);

        ssArgs.forceMessageKeys.put("!items", ("!chest" + iChestnumber));

        Block bHolder = checkChestAmount(ssArgs, iChestnumber);
        if(bHolder == null) {
            if(ssArgs.isOperationParameter("allowNoChests")) {
                List<Block> containables = new LinkedList<>();
                ssArgs.getContainables().set(containables);
                return true;
            }

            ssArgs.getPlayer().get().sendMessage("You need at least " + (iChestnumber) + " chest(s) to setup this shop!");
            return false;
        }

        List<Block> containables = new LinkedList<>();
        containables.add(bHolder);
        ssArgs.getContainables().set(containables);

        // In case the next operation doesn't write to !items, in other cases it will be overwritten (by f.e. takePlayerItems)
        ItemStack[] isTotalItems = itemUtil.getAllItemStacksForContainables(ssArgs.getContainables().get());
        if(isTotalItems.length > 0) {
            ssArgs.setMessagePart("!items", ItemMessagePart.fromItems(isTotalItems));
            ssArgs.miscSettings.put("chest" + iChestnumber, signshopUtil.implode(itemUtil.convertItemStacksToString(isTotalItems), separator));
        }

        // Since we'll be requesting the MetaID before the Seller is created, we need to register the items here
        Seller.storeMeta(itemUtil.getAllItemStacksForContainables(containables));

        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        return this.runOperation(ssArgs);
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        int iChestnumber;
        try {
            iChestnumber = Integer.parseInt(ssArgs.getFirstOperationParameter());
        } catch(NumberFormatException ex) {
            return incorrectPar(ssArgs);
        }
        if(iChestnumber < 1)
            return incorrectPar(ssArgs);
        if(!ssArgs.miscSettings.containsKey(("chest" + iChestnumber)))
            return incorrectPar(ssArgs);

        ssArgs.forceMessageKeys.put("!items", ("!chest" + iChestnumber));

        // Use cached deserialized items if seller reference is available (performance optimization)
        ItemStack[] isItemss;
        if (ssArgs.getSeller() != null) {
            isItemss = ssArgs.getSeller().getCachedMiscItems("chest" + iChestnumber);
        } else {
            // Fallback to manual deserialization if no seller reference (shouldn't happen normally)
            String misc = ssArgs.miscSettings.get(("chest" + iChestnumber));
            String[] sItemss;
            if(!misc.contains(SignShopArguments.separator)) {
                sItemss = new String[1];
                sItemss[0] = misc;
            } else
                sItemss = misc.split(SignShopArguments.separator);
            isItemss = itemUtil.convertStringtoItemStacks(Arrays.asList(sItemss));
        }

        // Phase 3B: Check for null items (incompatible items that failed to deserialize)
        if (itemUtil.hasNullItems(isItemss)) {
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("shop_has_incompatible_items", ssArgs.getMessageParts()));
            return false;
        }

        ssArgs.getItems().set(isItemss);
        // Set message part so !chest1, !chest2, etc. placeholders get replaced in transaction messages
        // Set directly (don't use forceMessageKeys) to avoid being overwritten by other operations
        ssArgs.setMessagePart("!chest" + iChestnumber, ItemMessagePart.fromItems(isItemss));

        Block bHolder = checkChestAmount(ssArgs, iChestnumber);
        if(bHolder != null) {
            LinkedList<Block> tempContainables = new LinkedList<>();
            tempContainables.add(bHolder);
            ssArgs.getContainables().set(tempContainables);
        }

        return true;
    }
}

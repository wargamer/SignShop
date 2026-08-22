package org.wargamer2010.signshop.operations;

import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.ItemMessagePart;
import org.wargamer2010.signshop.util.itemUtil;

/**
 * Shop operation that removes items from a player's inventory.
 * Used in sell shops and trade shops where players give items.
 */
public class takePlayerItems implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(ssArgs.getContainables().isEmpty()) {
            if(ssArgs.isOperationParameter("allowNoChests"))
                return true;
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("chest_missing", ssArgs.getMessageParts()));
            return false;
        }
        ItemStack[] isTotalItems = itemUtil.getAllItemStacksForContainables(ssArgs.getContainables().get());

        if(!ssArgs.isOperationParameter("allowemptychest") && isTotalItems.length == 0) {
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("chest_empty", ssArgs.getMessageParts()));
            return false;
        }
        if(isTotalItems.length > 0)
            ssArgs.getItems().set(isTotalItems);
        ssArgs.setMessagePart("!items", ItemMessagePart.fromItems(ssArgs.getItems().get()));
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(!ssArgs.isPlayerOnline())
            return true;
        if(ssArgs.getItems().get() == null) {
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("no_items_defined_for_shop", ssArgs.getMessageParts()));
            return false;
        }

        // Phase 3B: Check for null items within the array (incompatible items that failed to deserialize)
        if (itemUtil.hasNullItems(ssArgs.getItems().get())) {
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("shop_has_incompatible_items", ssArgs.getMessageParts()));
            return false;
        }

        SignShopPlayer player = ssArgs.getPlayer().get();
        ssArgs.setMessagePart("!items", ItemMessagePart.fromItems(ssArgs.getItems().get()));
        if(!player.getVirtualInventory().isStockOK(ssArgs.getItems().get(), true)) {
            ssArgs.sendFailedRequirementsMessage("player_doesnt_have_items");
            return false;
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        boolean transactedAll = ssArgs.getPlayer().get().takePlayerItems(ssArgs.getItems().get()).isEmpty();
        if(!transactedAll)
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("could_not_complete_operation", null));
        return transactedAll;
    }
}

package org.wargamer2010.signshop.events;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.SignShopPlayer;

import java.util.List;
import java.util.Map;

/**
 * Event fired after a successful shop transaction.
 */
public class SSPostTransactionEvent extends SSPreTransactionEvent {
    private static final HandlerList handlers = new HandlerList();

    public SSPostTransactionEvent(double pPrice,
                                ItemStack[] pItems,
                                List<Block> pContainables,
                                List<Block> pActivatables,
                                SignShopPlayer pPlayer,
                                SignShopPlayer pOwner,
                                Block pSign,
                                String pOperation,
                                Map<String, Object> pMessageParts,
                                Seller pShop,
                                Action pAction,
                                boolean pRequirementsOK) {
        super(pPrice, pItems, pContainables, pActivatables, pPlayer, pOwner, pSign, pOperation, pMessageParts, pShop, pAction, pRequirementsOK);
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

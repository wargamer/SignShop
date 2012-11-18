package org.wargamer2010.signshop.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class SSPostCreatedEvent extends SSPreCreatedEvent {
    private static final HandlerList handlers = new HandlerList();
    
    public SSPostCreatedEvent(Float pPrice, ItemStack[] pItems, List<Block> pContainables, List<Block> pActivatables, SignShopPlayer pPlayer, Block pSign, String pOperation, Map<String, String> pMessageParts) {
        super(pPrice, pItems, pContainables, pActivatables, pPlayer, pSign, pOperation, pMessageParts);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

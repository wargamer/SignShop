package org.wargamer2010.signshop.events;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class SSDestroyedEvent extends SSEvent {
    private static final HandlerList handlers = new HandlerList();

    private SignShopPlayer ssPlayer = null;
    private Block bBlock = null;
    private Seller seShop = null;
    private SSDestroyedEventType reason = SSDestroyedEventType.unknown;

    public SSDestroyedEvent(Block pBlock, SignShopPlayer pPlayer, Seller pShop, SSDestroyedEventType pReason) {
        ssPlayer = pPlayer;
        bBlock = pBlock;
        seShop = pShop;
        reason = pReason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public SignShopPlayer getPlayer() {
        return ssPlayer;
    }

    public Block getBlock() {
        return bBlock;
    }

    public Seller getShop() {
        return seShop;
    }

    public SSDestroyedEventType getReason() {
        return reason;
    }
}

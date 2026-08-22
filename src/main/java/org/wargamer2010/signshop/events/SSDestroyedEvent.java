package org.wargamer2010.signshop.events;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.data.Storage;
import org.wargamer2010.signshop.player.SignShopPlayer;

import java.util.Objects;

/**
 * Event fired when a SignShop is destroyed (sign broken, explosion, etc.).
 */
public class SSDestroyedEvent extends SSEvent {
    private static final HandlerList handlers = new HandlerList();

    private final SignShopPlayer ssPlayer;
    private final Block bBlock;
    private Seller seShop = null;
    private final SSDestroyedEventType reason;

    public SSDestroyedEvent(Block pBlock, SignShopPlayer pPlayer, Seller pShop, SSDestroyedEventType pReason) {
        ssPlayer = Objects.requireNonNullElseGet(pPlayer, SignShopPlayer::new);
        bBlock = pBlock;
        if(pShop != null)
            seShop = pShop;
        else if(pReason == SSDestroyedEventType.sign)
            seShop = Storage.get().getSeller(pBlock.getLocation());
        reason = pReason;
    }

    @NotNull
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

package org.wargamer2010.signshop.events;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class SSLinkEvent extends SSEvent {
    private static final HandlerList handlers = new HandlerList();

    private SignShopPlayer ssPlayer;
    private Block bBlock;
    private Seller seShop;

    public SSLinkEvent(Block pBlock, SignShopPlayer pPlayer, Seller pShop) {
        ssPlayer = pPlayer;
        bBlock = pBlock;
        seShop = pShop;
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
}

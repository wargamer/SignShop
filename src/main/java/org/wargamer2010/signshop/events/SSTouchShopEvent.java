package org.wargamer2010.signshop.events;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.jetbrains.annotations.NotNull;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class SSTouchShopEvent extends SSEvent {
    private static final HandlerList handlers = new HandlerList();

    private final SignShopPlayer ssPlayer;
    private final Seller seShop;
    private final Action aAction;
    private final Block bBlock;

    public SSTouchShopEvent(SignShopPlayer pPlayer, Seller pShop, Action pAction, Block pBlock) {
        ssPlayer = pPlayer;
        seShop = pShop;
        aAction = pAction;
        bBlock = pBlock;
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

    public Seller getShop() {
        return seShop;
    }

    public Action getAction() {
        return aAction;
    }

    public Block getBlock() {
        return bBlock;
    }


}

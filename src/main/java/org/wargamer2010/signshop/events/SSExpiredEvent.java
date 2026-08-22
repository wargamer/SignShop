package org.wargamer2010.signshop.events;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.wargamer2010.signshop.timing.IExpirable;

public class SSExpiredEvent extends SSEvent {
    private static final HandlerList handlers = new HandlerList();

    private final IExpirable expirable;

    public SSExpiredEvent(IExpirable pExpirable) {
        expirable = pExpirable;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public IExpirable getExpirable() {
        return expirable;
    }
}

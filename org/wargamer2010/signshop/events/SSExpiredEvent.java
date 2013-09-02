package org.wargamer2010.signshop.events;

import org.bukkit.event.HandlerList;
import org.wargamer2010.signshop.timing.IExpirable;

public class SSExpiredEvent extends SSEvent {
    private static final HandlerList handlers = new HandlerList();

    private IExpirable expirable = null;

    public SSExpiredEvent(IExpirable pExpirable) {
        expirable = pExpirable;
    }

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

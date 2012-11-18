
package org.wargamer2010.signshop.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class SSEvent extends Event implements Cancellable {
    private boolean bCancelled = false;

    @Override
    public boolean isCancelled() {
        return bCancelled;
    }

    @Override
    public void setCancelled(boolean pCancelled) {
        bCancelled = pCancelled;
    }

    @Override
    public abstract HandlerList getHandlers();

    public static HandlerList getHandlerList() {
        return null;
    }
}


package org.wargamer2010.signshop.events;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class SSEvent extends Event implements Cancellable {
    private boolean bCancelled = false;
    private boolean bCanBeCancelled = true;
    private Map<String, String> messageParts = new HashMap<String, String>();

    public SSEvent() {

    }

    public SSEvent(Map<String, String> pMessageParts) {
        messageParts = pMessageParts;
    }

    @Override
    public boolean isCancelled() {
        return bCancelled;
    }

    @Override
    public void setCancelled(boolean pCancelled) {
        if(!bCanBeCancelled)
            return;
        bCancelled = pCancelled;
    }

    public boolean canBeCancelled() {
        return bCanBeCancelled;
    }

    public void setCanBeCancelled(boolean pCanBeCancelled) {
        this.bCanBeCancelled = pCanBeCancelled;
    }

    public Map<String, String> getMessageParts() {
        return messageParts;
    }

    public void setMessagePart(String part, String value) {
        messageParts.put(part, value);
    }

    @Override
    public abstract HandlerList getHandlers();

    public static HandlerList getHandlerList() {
        return null;
    }
}

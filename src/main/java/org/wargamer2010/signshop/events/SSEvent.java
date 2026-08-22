
package org.wargamer2010.signshop.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all SignShop internal events.
 *
 * <p>Extends Bukkit's Event system with message parts for template substitution
 * and cancellation support. All SignShop events (creation, transaction, destruction)
 * extend this class.</p>
 *
 * @see SSCreatedEvent
 * @see SSPreTransactionEvent
 * @see SSPostTransactionEvent
 * @see SSMoneyTransactionEvent
 */
public abstract class SSEvent extends Event implements Cancellable {
    private boolean bCancelled = false;
    private boolean bCanBeCancelled = true;
    private Map<String, Object> messageParts = new HashMap<>();

    public SSEvent() {

    }

    public SSEvent(Map<String, Object> pMessageParts) {
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

    public Map<String, Object> getMessageParts() {
        return messageParts;
    }

    public void setMessagePart(String part, Object value) {
        messageParts.put(part, value);
    }

    /**
     * Overloaded method for binary compatibility with external plugins.
     *
     * @param part The message part key
     * @param value The string value
     */
    public void setMessagePart(String part, String value) {
        setMessagePart(part, (Object) value);
    }

    @NotNull
    @Override
    public abstract HandlerList getHandlers();

    public static HandlerList getHandlerList() {
        return null;
    }
}

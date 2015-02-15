
package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.events.SSPostTransactionEvent;
import org.wargamer2010.signshop.events.SSCreatedEvent;

public class SimpleMessenger implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSSBuildEvent(SSCreatedEvent event) {
        if(event.isCancelled())
            return;
        event.getPlayer().sendMessage(SignShopConfig.getMessage("setup", event.getOperation(), event.getMessageParts()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSPostTransactionEvent(SSPostTransactionEvent event) {
        if(event.isCancelled())
            return;
        event.getPlayer().sendMessage(SignShopConfig.getMessage("transaction", event.getOperation(), event.getMessageParts()));
        event.getOwner().sendMessage(SignShopConfig.getMessage("transaction_owner", event.getOperation(), event.getMessageParts()));
    }
}

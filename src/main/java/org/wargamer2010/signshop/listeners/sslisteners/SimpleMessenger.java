
package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.events.SSPostTransactionEvent;

public class SimpleMessenger implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSSBuildEvent(SSCreatedEvent event) {
        if(event.isCancelled())
            return;
        event.getPlayer().sendMessage(SignShop.getInstance().getSignShopConfig().getMessage("setup", event.getOperation(), event.getMessageParts()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSPostTransactionEvent(SSPostTransactionEvent event) {
        if (event.isCancelled())
            return;
        event.getPlayer().sendMessage(SignShop.getInstance().getSignShopConfig().getMessage("transaction", event.getOperation(), event.getMessageParts()));
        event.getOwner().sendMessage(SignShop.getInstance().getSignShopConfig().getMessage("transaction_owner", event.getOperation(), event.getMessageParts()));
    }
}

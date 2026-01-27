
package org.wargamer2010.signshop.listeners.sslisteners;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.events.SSPostTransactionEvent;

/**
 * Sends transaction messages to players.
 *
 * <p>Responds to creation and transaction events to send configured
 * messages with item hover tooltips and price information.</p>
 */
public class SimpleMessenger implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSSBuildEvent(SSCreatedEvent event) {
        if(event.isCancelled())
            return;
        // Use component-based message for rich hover tooltips on items
        BaseComponent message = SignShop.getInstance().getSignShopConfig()
                .getMessageAsComponent("setup", event.getOperation(), event.getMessageParts());
        event.getPlayer().sendMessage(message);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSPostTransactionEvent(SSPostTransactionEvent event) {
        if (event.isCancelled())
            return;
        // Use component-based messages for rich hover tooltips on items
        BaseComponent playerMessage = SignShop.getInstance().getSignShopConfig()
                .getMessageAsComponent("transaction", event.getOperation(), event.getMessageParts());
        event.getPlayer().sendMessage(playerMessage);

        BaseComponent ownerMessage = SignShop.getInstance().getSignShopConfig()
                .getMessageAsComponent("transaction_owner", event.getOperation(), event.getMessageParts());
        event.getOwner().sendMessage(ownerMessage);
    }
}

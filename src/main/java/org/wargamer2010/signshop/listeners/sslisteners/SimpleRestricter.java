package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.events.SSPreTransactionEvent;
import org.wargamer2010.signshop.util.signshopUtil;

/**
 * Internal listener that restricts shop usage based on RestrictedSign metadata to enforce player restrictions.
 */
public class SimpleRestricter implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSPreTransactionEvent(SSPreTransactionEvent event) {
        if(event.isCancelled() || !event.canBeCancelled())
            return;
        if(signshopUtil.restrictedFromUsing(event.getShop(), event.getPlayer())) {
            event.getPlayer().sendMessage(SignShop.getInstance().getSignShopConfig().getError("restricted_from_using", null));
            event.setCancelled(true);
        }
    }
}

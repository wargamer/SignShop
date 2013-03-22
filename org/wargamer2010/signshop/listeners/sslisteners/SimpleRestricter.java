
package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.events.SSPreTransactionEvent;
import org.wargamer2010.signshop.util.signshopUtil;

public class SimpleRestricter implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSPreTransactionEvent(SSPreTransactionEvent event) {
        if(event.isCancelled() || !event.canBeCancelled())
            return;
        if(signshopUtil.restrictedFromUsing(event.getShop(), event.getPlayer())) {
            event.getPlayer().sendMessage(SignShopConfig.getError("restricted_from_using", null));
            event.setCancelled(true);
        }
    }
}

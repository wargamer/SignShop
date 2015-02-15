
package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.events.SSLinkEvent;
import org.wargamer2010.signshop.hooks.HookManager;

public class SimpleBlockProtector implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSLinkEvent(SSLinkEvent event) {
        if(event.isCancelled() || !event.canBeCancelled())
            return;
        if(!HookManager.canBuild(event.getPlayer().getPlayer(), event.getBlock())) {
            event.getPlayer().sendMessage(SignShopConfig.getError("link_notallowed", null));
            event.setCancelled(true);
        }
    }
}

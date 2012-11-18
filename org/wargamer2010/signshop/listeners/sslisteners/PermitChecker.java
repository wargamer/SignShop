
package org.wargamer2010.signshop.listeners.sslisteners;

import java.util.List;
import java.util.Map;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.events.SSPreCreatedEvent;
import org.wargamer2010.signshop.events.SSPreTransactionEvent;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.itemUtil;

public class PermitChecker implements Listener {

    private boolean hasPermit(SignShopPlayer ssPlayer, List<String> operation) {
        return !(SignShopConfig.getEnablePermits() && !operation.contains("playerIsOp") && !ssPlayer.hasPerm("SignShop.Permit", true));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSBuildEvent(SSPreCreatedEvent event) {
        if(event.isCancelled())
            return;
        List<String> operation = SignShopConfig.getBlocks(event.getOperation());
        if(!hasPermit(event.getPlayer(), operation)) {
            event.getPlayer().sendMessage(SignShopConfig.getError("need_permit", null));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSPreTransactionEvent(SSPreTransactionEvent event) {
        if(event.isCancelled())
            return;
        List<String> operation = SignShopConfig.getBlocks(event.getOperation());
        if(!hasPermit(event.getOwner(), operation)) {
            event.getPlayer().sendMessage(SignShopConfig.getError("no_permit_owner", null));
            event.setCancelled(true);
        }
    }
}

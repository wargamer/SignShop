
package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.events.SSPreTransactionEvent;
import org.wargamer2010.signshop.player.SignShopPlayer;

import java.util.List;

public class PermissionChecker implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSBuildEvent(SSCreatedEvent event) {
        if(event.isCancelled() || !event.canBeCancelled())
            return;
        List<String> operation = SignShop.getInstance().getSignShopConfig().getBlocks(event.getOperation());
        String sOperation = event.getOperation();
        if(!operation.contains("playerIsOp") && !event.getPlayer().hasPerm(("SignShop.Signs."+sOperation), false) && !event.getPlayer().hasPerm(("SignShop.Signs.*"), false)) {
            event.getPlayer().sendMessage(SignShop.getInstance().getSignShopConfig().getError("no_permission", null));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSPreTransactionEvent(SSPreTransactionEvent event) {
        if(event.isCancelled() || !event.canBeCancelled())
            return;
        String sOperation = event.getOperation();
        SignShopPlayer ssPlayer = event.getPlayer();
        if(ssPlayer.hasPerm(("SignShop.DenyUse."+sOperation), false) && !ssPlayer.hasPerm(("SignShop.Signs."+sOperation), false) && !ssPlayer.hasPerm(("SignShop.Admin."+sOperation), true)) {
            ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("no_permission_use", null));
            event.setCancelled(true);
        }
    }
}

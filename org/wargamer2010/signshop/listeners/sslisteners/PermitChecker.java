
package org.wargamer2010.signshop.listeners.sslisteners;

import java.util.List;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.events.SSPreTransactionEvent;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class PermitChecker implements Listener {

    private boolean hasPermit(SignShopPlayer ssPlayer, List<String> operation, World world, ItemStack[] stacks) {
        if (!SignShopConfig.getEnablePermits() || operation.contains("playerIsOp"))
            return true;
        if(ssPlayer.hasPerm("SignShop.Permit", world, true))
            return true;
        for(ItemStack stack : stacks) {
            if(!ssPlayer.hasPerm("SignShop.Permit." + stack.getType().toString(), world, true))
                return false;
        }

        if(stacks.length == 0)
            return false;

        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSBuildEvent(SSCreatedEvent event) {
        if(event.isCancelled() || !event.canBeCancelled())
            return;
        List<String> operation = SignShopConfig.getBlocks(event.getOperation());
        if(!hasPermit(event.getPlayer(), operation, event.getPlayer().getWorld(), event.getItems())) {
            event.getPlayer().sendMessage(SignShopConfig.getError("need_permit", null));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSPreTransactionEvent(SSPreTransactionEvent event) {
        if(event.isCancelled() || !event.canBeCancelled())
            return;
        List<String> operation = SignShopConfig.getBlocks(event.getOperation());
        if(!hasPermit(event.getOwner(), operation, event.getPlayer().getWorld(), event.getShop().getItems())) {
            event.getPlayer().sendMessage(SignShopConfig.getError("no_permit_owner", null));
            event.setCancelled(true);
        }
    }
}

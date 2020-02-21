
package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.events.SSPreTransactionEvent;
import org.wargamer2010.signshop.player.SignShopPlayer;

import java.util.List;

public class PermitChecker implements Listener {

    private boolean hasNoPermit(SignShopPlayer ssPlayer, List<String> operation, World world, ItemStack[] stacks) {
        if (!SignShopConfig.getEnablePermits() || operation.contains("playerIsOp"))
            return false;
        if(ssPlayer.hasPerm("SignShop.Permit", world, true))
            return false;
        for(ItemStack stack : stacks) {
            if(!ssPlayer.hasPerm("SignShop.Permit." + stack.getType().toString(), world, true))
                return true;
        }

        return stacks.length == 0;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSBuildEvent(SSCreatedEvent event) {
        if(event.isCancelled() || !event.canBeCancelled())
            return;
        List<String> operation = SignShopConfig.getBlocks(event.getOperation());
        if (hasNoPermit(event.getPlayer(), operation, event.getPlayer().getWorld(), event.getItems())) {
            event.getPlayer().sendMessage(SignShopConfig.getError("need_permit", null));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSPreTransactionEvent(SSPreTransactionEvent event) {
        if(event.isCancelled() || !event.canBeCancelled())
            return;
        List<String> operation = SignShopConfig.getBlocks(event.getOperation());
        if (hasNoPermit(event.getOwner(), operation, event.getPlayer().getWorld(), event.getShop().getItems())) {
            event.getPlayer().sendMessage(SignShopConfig.getError("no_permit_owner", null));
            event.setCancelled(true);
        }
    }
}

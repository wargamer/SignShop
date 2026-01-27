
package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.events.SSPreTransactionEvent;
import org.wargamer2010.signshop.player.SignShopPlayer;

import java.util.List;

/**
 * Internal listener that enforces permit requirements for creating and using shops with specific item types.
 */
public class PermitChecker implements Listener {

    private boolean hasNoPermit(SignShopPlayer ssPlayer, List<String> operation, World world, ItemStack[] stacks) {
        if (!SignShop.getInstance().getSignShopConfig().getEnablePermits() || operation.contains("playerIsOp"))
            return false;
        if (ssPlayer.hasPerm("SignShop.Permit", world, true))
            return false;
        for(ItemStack stack : stacks) {
            if(!ssPlayer.hasPerm("SignShop.Permit." + stack.getType(), world, true))
                return true;
        }

        return stacks.length == 0;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSBuildEvent(SSCreatedEvent event) {
        if(event.isCancelled() || !event.canBeCancelled())
            return;
        List<String> operation = SignShop.getInstance().getSignShopConfig().getBlocks(event.getOperation());
        if (hasNoPermit(event.getPlayer(), operation, event.getPlayer().getWorld(), event.getItems())) {
            event.getPlayer().sendMessage(SignShop.getInstance().getSignShopConfig().getError("need_permit", null));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSPreTransactionEvent(SSPreTransactionEvent event) {
        if(event.isCancelled() || !event.canBeCancelled())
            return;
        List<String> operation = SignShop.getInstance().getSignShopConfig().getBlocks(event.getOperation());
        if (hasNoPermit(event.getOwner(), operation, event.getPlayer().getWorld(), event.getShop().getItems())) {
            event.getPlayer().sendMessage(SignShop.getInstance().getSignShopConfig().getError("no_permit_owner", null));
            event.setCancelled(true);
        }
    }
}

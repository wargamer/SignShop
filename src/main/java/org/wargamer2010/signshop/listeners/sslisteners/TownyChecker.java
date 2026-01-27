
package org.wargamer2010.signshop.listeners.sslisteners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.hooks.HookManager;

/**
 * Internal listener that enforces Towny's commercial plot requirement for shop creation unless bypassed.
 */
public class TownyChecker implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSBuildEvent(SSCreatedEvent event) {
        if(event.isCancelled() || !event.canBeCancelled() || event.getPlayer().getPlayer() == null)
            return;
        if(HookManager.getHook("Towny") == null)
            return;

        // Towny will take of plot ownership so no need to check that here
        TownBlock block = TownyAPI.getInstance().getTownBlock(event.getSign().getLocation());
        if(block == null || block.getType() != TownBlockType.COMMERCIAL) { // Commercial == ShopPlot
            if(event.getPlayer().isOp())
                event.getPlayer().sendMessage(SignShop.getInstance().getSignShopConfig().getError("towny_shop_plot_not_allowed_but_op", event.getMessageParts()));
            else if(event.getPlayer().hasBypassShopPlots("Towny"))
                event.getPlayer().sendMessage(SignShop.getInstance().getSignShopConfig().getError("towny_shop_plot_not_allowed_but_perm", event.getMessageParts()));
            else {
                event.getPlayer().sendMessage(SignShop.getInstance().getSignShopConfig().getError("towny_shop_plot_not_allowed", event.getMessageParts()));
                event.setCancelled(true);
            }
        }
    }
}

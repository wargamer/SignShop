
package org.wargamer2010.signshop.listeners.sslisteners;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.hooks.HookManager;

public class TownyChecker implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSBuildEvent(SSCreatedEvent event) {
        if(event.isCancelled() || !event.canBeCancelled() || event.getPlayer().getPlayer() == null)
            return;
        if(HookManager.getHook("Towny") == null)
            return;

        TownBlock block = TownyUniverse.getTownBlock(event.getSign().getLocation());
        if(block == null || block.getType() != TownBlockType.COMMERCIAL) // Commercial == ShopPlot
            return;

        // If the player is not part of a Town, not registered as a Resident or part of the wrong town
        // we'll assume Towny has already properly handled it so we we only check for permission here
        if(event.getPlayer().hasPerm("SignShop.Towny.BuildOnShopPlot", true))
            return;
        if(event.getPlayer().isOp())
            event.getPlayer().sendMessage(SignShopConfig.getError("towny_shop_plot_not_allowed_but_op", event.getMessageParts()));
        else if(event.getPlayer().hasBypassShopPlots("Towny"))
            event.getPlayer().sendMessage(SignShopConfig.getError("towny_shop_plot_not_allowed_but_perm", event.getMessageParts()));
        else {
            event.getPlayer().sendMessage(SignShopConfig.getError("towny_shop_plot_not_allowed", event.getMessageParts()));
            event.setCancelled(true);
        }
    }
}

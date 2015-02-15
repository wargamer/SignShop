
package org.wargamer2010.signshop.listeners.sslisteners;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

public class SimpleShopLimiter implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSBuildEvent(SSCreatedEvent event) {
        if(event.isCancelled() || !event.canBeCancelled())
            return;
        int iLimit = event.getPlayer().reachedMaxShops();
        if(!SignShopConfig.getBlocks(event.getOperation()).contains("playerIsOp") && iLimit > 0) {
            event.getPlayer().sendMessage(SignShopConfig.getError("too_many_shops", null).replace("!max", Integer.toString(iLimit)));
            itemUtil.setSignStatus(event.getSign(), ChatColor.BLACK);
            event.setCancelled(true);
            return;
        }

        List<String> operation = SignShopConfig.getBlocks(event.getOperation());
        Block bClicked = event.getSign();

        for(Block bCheckme : event.getContainables()) {
            if(event.getSign().getWorld().getName().equals(bCheckme.getWorld().getName())) {
               if(!signshopUtil.checkDistance(bClicked, bCheckme, SignShopConfig.getMaxSellDistance()) && !operation.contains("playerIsOp")) {
                   event.setMessagePart("!max", Integer.toString(SignShopConfig.getMaxSellDistance()));
                   event.getPlayer().sendMessage(SignShopConfig.getError("too_far", event.getMessageParts()));
                   event.setCancelled(true);
                   return;
               }
           }
       }
    }
}

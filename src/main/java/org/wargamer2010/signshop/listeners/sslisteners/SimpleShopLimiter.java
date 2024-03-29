
package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.List;

public class SimpleShopLimiter implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSBuildEvent(SSCreatedEvent event) {
        if (event.isCancelled() || !event.canBeCancelled())
            return;
        int iLimit = event.getPlayer().reachedMaxShops();
        if (!SignShop.getInstance().getSignShopConfig().getBlocks(event.getOperation()).contains("playerIsOp") && iLimit > 0) {
            event.getPlayer().sendMessage(SignShop.getInstance().getSignShopConfig().getError("too_many_shops", null).replace("!max", Integer.toString(iLimit)));
            itemUtil.setSignStatus(event.getSign(), ChatColor.BLACK);
            event.setCancelled(true);
            return;
        }

        List<String> operation = SignShop.getInstance().getSignShopConfig().getBlocks(event.getOperation());
        Block bClicked = event.getSign();

        for (Block bCheckme : event.getContainables()) {
            if (event.getSign().getWorld().getName().equals(bCheckme.getWorld().getName())) {
                if (!signshopUtil.checkDistance(bClicked, bCheckme, SignShop.getInstance().getSignShopConfig().getMaxSellDistance()) && !operation.contains("playerIsOp")) {
                    event.setMessagePart("!max", Integer.toString(SignShop.getInstance().getSignShopConfig().getMaxSellDistance()));
                    event.getPlayer().sendMessage(SignShop.getInstance().getSignShopConfig().getError("too_far", event.getMessageParts()));
                    event.setCancelled(true);
                    return;
                }
            }
       }
    }
}

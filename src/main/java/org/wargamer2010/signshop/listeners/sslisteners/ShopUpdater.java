
package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.InventoryHolder;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.events.SSTouchShopEvent;
import org.wargamer2010.signshop.util.itemUtil;

public class ShopUpdater implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSSPostCreatedEvent(SSCreatedEvent event) {
        if(!event.isCancelled())
            itemUtil.setSignStatus(event.getSign(), ChatColor.DARK_BLUE);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSTouchShopEvent(SSTouchShopEvent event) {
        if(event.isCancelled())
            return;

        if(event.getAction() == Action.LEFT_CLICK_BLOCK && event.getBlock().getState() instanceof InventoryHolder) {
            itemUtil.updateStockStatusPerShop(event.getShop());
        }
    }
}

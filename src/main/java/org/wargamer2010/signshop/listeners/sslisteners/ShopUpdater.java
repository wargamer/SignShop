
package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.InventoryHolder;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.events.SSTouchShopEvent;
import org.wargamer2010.signshop.util.itemUtil;

/**
 * Internal listener that updates sign colors based on stock status when shops are created or touched.
 */
public class ShopUpdater implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSSPostCreatedEvent(SSCreatedEvent event) {
        if(!event.isCancelled())
            itemUtil.setSignStatus(event.getSign(), SignShop.getInstance().getSignShopConfig().getInStockColor());
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

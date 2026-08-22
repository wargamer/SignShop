
package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.events.SSPreTransactionEvent;
import org.wargamer2010.signshop.util.itemUtil;

import java.util.List;

/**
 * Updates shop sign colors based on stock availability.
 *
 * <p>When a shop owner left-clicks their shop, this listener checks if the
 * shop is stocked and updates the sign color (blue = stocked, red = out of stock).</p>
 */
public class StockChecker implements Listener {
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSSPreTransactionEvent(SSPreTransactionEvent event) {
        if(event.isCancelled() || event.getAction() != Action.LEFT_CLICK_BLOCK || !event.getPlayer().isOwner(event.getShop()))
            return;
        
        if(!event.getMessageParts().containsKey("!price") || !event.getMessageParts().containsKey("!items") || event.getContainables().isEmpty())
            return;

        List<String> operation = SignShop.getInstance().getSignShopConfig().getBlocks(event.getOperation());

        // We'll assume that all admin shops don't have stock that needs to be checked.
        if(operation.contains("playerIsOp"))
            return;
        
        ItemStack[] allStacks = itemUtil.getAllItemStacksForContainables(event.getShop().getContainables());
        ItemStack[] filtered = itemUtil.filterStacks(allStacks, event.getShop().getItems());

        if (filtered.length == 0) {
            event.setMessagePart("!shopinventory", "nothing");
        } else {
            event.setMessagePart("!shopinventory", itemUtil.itemStackToString(filtered));
        }
        event.getPlayer().sendMessage(SignShop.getInstance().getSignShopConfig().getError("shop_contains", event.getMessageParts()));
    }
}

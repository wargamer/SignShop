
package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.events.SSPreTransactionEvent;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.itemUtil;

import java.util.Map;

/**
 * Internal listener that blocks blacklisted items from being used in shop creation and transactions.
 */
public class SimpleBlacklister implements Listener {

    private boolean runBlacklistCheck(ItemStack[] isItems, SignShopPlayer ssPlayer, Map<String, Object> messageParts) {
        if(isItems == null)
            return false;
        ItemStack blacklisted = SignShop.getInstance().getSignShopConfig().isAnyItemOnBlacklist(isItems);
        if(blacklisted != null) {
            messageParts.put("!blacklisted_item", itemUtil.itemStackToString(new ItemStack[]{blacklisted}));

            if (ssPlayer.isOp()) {
                if (SignShop.getInstance().getSignShopConfig().getUseBlacklistAsWhitelist())
                    ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("item_not_on_whitelist_but_op", messageParts));
                else
                    ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("item_on_blacklist_but_op", messageParts));
                return false;
            }

            if (SignShop.getInstance().getSignShopConfig().getUseBlacklistAsWhitelist())
                ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("item_not_on_whitelist", messageParts));
            else
                ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("item_on_blacklist", messageParts));
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSBuildEvent(SSCreatedEvent event) {
        if(event.isCancelled() || !event.canBeCancelled())
            return;
        boolean isBlacklisted = this.runBlacklistCheck(event.getItems(), event.getPlayer(), event.getMessageParts());
        if(isBlacklisted)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSPreTransactionEvent(SSPreTransactionEvent event) {
        if(event.isCancelled() || !event.canBeCancelled())
            return;
        boolean isBlacklisted = this.runBlacklistCheck(event.getItems(), event.getPlayer(), event.getMessageParts());
        if(isBlacklisted)
            event.setCancelled(true);
    }
}

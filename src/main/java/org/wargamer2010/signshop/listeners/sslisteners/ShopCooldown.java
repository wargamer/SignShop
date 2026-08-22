
package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.events.SSPreTransactionEvent;
import org.wargamer2010.signshop.util.CooldownUtil;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Internal listener that enforces global shop usage cooldown per player to prevent spam.
 */
public class ShopCooldown implements Listener {
    private static final Map<String, Long> lastusedByPlayer = new LinkedHashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSPreTransactionEvent(SSPreTransactionEvent event) {
        if(event.isCancelled() || event.getAction() != Action.RIGHT_CLICK_BLOCK || !event.getRequirementsOK())
            return;
        int cooldown = SignShop.getInstance().getSignShopConfig().getShopCooldown();
        long now = new Date().getTime();
        String playername = event.getPlayer().getName();

        if(cooldown == 0)
            return;
        if(!lastusedByPlayer.containsKey(playername)) {
            lastusedByPlayer.put(playername, now);
            return;
        }

        long lastused = lastusedByPlayer.get(playername);
        long left = (((lastused + cooldown) - now) / 1000);

        if((now - lastused) < cooldown) {
            CooldownUtil.setCooldownMessage(event, left);
            event.getPlayer().sendMessage(SignShop.getInstance().getSignShopConfig().getError("shop_on_cooldown", event.getMessageParts()));
            event.setCancelled(true);
            return;
        }

        lastusedByPlayer.put(playername, now);
    }
}

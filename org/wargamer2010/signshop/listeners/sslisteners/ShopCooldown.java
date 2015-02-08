
package org.wargamer2010.signshop.listeners.sslisteners;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.events.SSPreTransactionEvent;

public class ShopCooldown implements Listener {
    private static Map<String, Long> lastusedByPlayer = new LinkedHashMap<String, Long>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSPreTransactionEvent(SSPreTransactionEvent event) {
        if(event.isCancelled() || event.getAction() != Action.RIGHT_CLICK_BLOCK || !event.getRequirementsOK())
            return;
        int cooldown = SignShopConfig.getShopCooldown();
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
            if(left >= 1)
                event.setMessagePart("!cooldownleft", Long.toString(left));
            else
                event.setMessagePart("!cooldownleft", "< 1");
                
            if(left >= 60)
                event.setMessagePart("!cooldownminutesleft", Long.toString(left/60));
            else
                event.setMessagePart("!cooldownminutesleft", "< 1");
            event.getPlayer().sendMessage(SignShopConfig.getError("shop_on_cooldown", event.getMessageParts()));
            event.setCancelled(true);
            return;
        }

        lastusedByPlayer.put(playername, now);
    }
}

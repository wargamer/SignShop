
package org.wargamer2010.signshop.listeners.sslisteners;

import java.awt.Color;
import me.muizers.Notifications.Notification;
import me.muizers.Notifications.Notifications;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.util.signshopUtil;

public class NotificationsHooker implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSSBuildEvent(SSCreatedEvent event) {
        if(event.isCancelled())
            return;
        Plugin pl = Bukkit.getPluginManager().getPlugin("Notifications");
        if(pl == null)
            return;
        String op = signshopUtil.getOperation((Sign) event.getSign().getState(), false);

        event.setMessagePart("!signtype", op);
        event.setMessagePart("!x", Integer.toString(event.getSign().getX()));
        event.setMessagePart("!y", Integer.toString(event.getSign().getY()));
        event.setMessagePart("!z", Integer.toString(event.getSign().getZ()));
        String message = SignShopConfig.getError("notifications_shop_built", event.getMessageParts());
        if(message.isEmpty())
            return;

        String[] temparr = message.split(" ");
        StringBuilder firstbit = new StringBuilder(message.length());
        StringBuilder secondbit = new StringBuilder(message.length());

        boolean isFirst = true;
        int half = Math.round(temparr.length / 2);
        for(int counter = 0; counter < temparr.length; counter++) {
            StringBuilder tempbuilder;
            if(counter < half)
                tempbuilder = firstbit;
            else
                tempbuilder = secondbit;
            if(isFirst || counter == half)
                isFirst = false;
            else
                tempbuilder.append(" ");

            tempbuilder.append(temparr[counter]);
        }

        Notifications notifications = (Notifications) pl;
        Notification not = new Notification("SignShop", firstbit.toString(), secondbit.toString(), Color.ORANGE, Color.WHITE, Color.WHITE);
        notifications.showNotification(not);
    }
}

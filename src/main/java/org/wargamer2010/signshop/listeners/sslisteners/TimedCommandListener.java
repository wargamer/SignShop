
package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.events.SSExpiredEvent;
import org.wargamer2010.signshop.timing.TimedCommand;

import java.util.List;
import java.util.logging.Level;

public class TimedCommandListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onRentExpired(SSExpiredEvent event) {
        String className = TimedCommand.class.getName();
        if(event.getExpirable().getName().equals(className)) {
            TimedCommand cmd = (TimedCommand) event.getExpirable();
            if (SignShop.getInstance().getSignShopConfig().getDelayedCommands().containsKey(cmd.getShopType().toLowerCase())) {
                List<String> commands = SignShop.getInstance().getSignShopConfig().getDelayedCommands().get(cmd.getShopType().toLowerCase());
                for (String command : commands) {
                    String sCommand = command;
                    if (sCommand != null && sCommand.length() > 0) {
                        sCommand = SignShop.getInstance().getSignShopConfig().fillInBlanks(sCommand, cmd.getMessageParts());
                        sCommand = SignShop.getInstance().getSignShopConfig().fillInBlanks(sCommand, cmd.getMessageParts());
                        if (cmd.getCommandType().equals("asOriginalUser"))
                            SignShop.log("Delayed commands can not be run asOriginalUser, shop type: " + cmd.getShopType(), Level.WARNING);
                        if (cmd.getCommandType().equals("asUser"))
                            SignShop.log("Delayed commands can not be run asUser, shop type: " + cmd.getShopType(), Level.WARNING);
                        else
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), sCommand);
                    }
                }
            }
        }
    }
}


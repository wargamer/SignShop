
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

/**
 * Internal listener that executes delayed commands from config when timed operations expire.
 */
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
                    if (sCommand != null && !sCommand.isEmpty()) {
                        sCommand = SignShop.getInstance().getSignShopConfig().fillInBlanks(sCommand, cmd.getMessagePartsAsObject());
                        sCommand = SignShop.getInstance().getSignShopConfig().fillInBlanks(sCommand, cmd.getMessagePartsAsObject());
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


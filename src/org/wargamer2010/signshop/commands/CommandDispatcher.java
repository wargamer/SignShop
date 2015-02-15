
package org.wargamer2010.signshop.commands;

import java.util.LinkedHashMap;
import java.util.Map;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class CommandDispatcher {
    private Map<String, ICommandHandler> handlers = new LinkedHashMap<String, ICommandHandler>();

    public synchronized void registerHandler(String commandName, ICommandHandler handler) {
        handlers.put(commandName, handler);
    }

    public boolean handle(String command, String[] args, SignShopPlayer player) {
        String lower = command.toLowerCase();
        return (handlers.containsKey(lower))
                ? handlers.get(lower).handle(lower, args, player)
                : handlers.get("").handle("", args, player);
    }
}

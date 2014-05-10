
package org.wargamer2010.signshop.commands;

import java.util.LinkedHashMap;
import java.util.Map;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class CommandDispatcher {
    private static Map<String, ICommandHandler> handlers = new LinkedHashMap<String, ICommandHandler>();

    private CommandDispatcher() {

    }

    public synchronized static void init() {
        if(!handlers.isEmpty())
            return;
        handlers.put("stats", StatsHandler.getInstance());
        handlers.put("version", StatsHandler.getInstance());
        handlers.put("about", StatsHandler.getInstance());
        handlers.put("reload", ReloadHandler.getInstance());
        handlers.put("tutorial", TutorialHandler.getInstance());
        handlers.put("help", HelpHandler.getInstance());
        handlers.put("sign", HelpHandler.getInstance());
        handlers.put("list", HelpHandler.getInstance());
        handlers.put("unlink", UnlinkHandler.getInstance());
        handlers.put("", HelpHandler.getInstance());
    }

    public static boolean handle(String command, String[] args, SignShopPlayer player) {
        String lower = command.toLowerCase();
        return (handlers.containsKey(lower))
                ? handlers.get(lower).handle(lower, args, player)
                : handlers.get("").handle("", args, player);
    }
}

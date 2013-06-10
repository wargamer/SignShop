
package org.wargamer2010.signshop.commands;

import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.commandUtil;

public class TutorialHandler implements ICommandHandler {
    private static ICommandHandler instance = new TutorialHandler();

    private TutorialHandler() {

    }

    public static ICommandHandler getInstance() {
        return instance;
    }

    @Override
    public boolean handle(String command, String[] args, SignShopPlayer player) {
        StringBuilder messageBuilder = new StringBuilder(200);

        if(args.length == 0 || (!args[0].equals("on") && !args[0].equals("off"))) {
            messageBuilder.append("Usage: /signshop ");
            messageBuilder.append(command);
            messageBuilder.append(" [on|off]");
        } else {
            boolean on = (args[0].equals("on"));
            if(on) {
                messageBuilder.append("Tutorial enabled.");
                player.removeMetaByPrefix("help_");
            } else {
                messageBuilder.append("Tutorial disabled.");
                player.setMeta("help_anyhelp", "1");
            }
        }

        commandUtil.sendToPlayerOrConsole(messageBuilder.toString(), player);
        return true;
    }

}

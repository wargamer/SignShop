
package org.wargamer2010.signshop.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class commandUtil {
    private static final String RootCommand = "signshop";

    private commandUtil() {

    }

    public static void sendToPlayerOrConsole(String message, SignShopPlayer player) {
        if(player != null)
            player.sendMessage(message);
        else
            SignShop.log(message, Level.INFO);
    }

    public static Collection<String> getCollectionFromSingle(String single) {
        Collection<String> coll = new LinkedList<String>();
        coll.add(single);
        return coll;
    }

    public static String getMessageForSubs(Collection<String> subCommands, String... preCommands) {
        return getCommandList(subCommands, "Available Subcommands: ", "\n", true, preCommands);
    }

    public static String getCommandList(Collection<String> subCommands, String header, String delimiter, boolean prefixRoot, String... preCommands) {
        StringBuilder builder = new StringBuilder(subCommands.size() * 25);
        builder.append(header);
        builder.append("\n");

        StringBuilder rootBuilder = new StringBuilder(preCommands.length * 25);
        for(String pre : preCommands) {
            if(rootBuilder.length() == 0) {
                rootBuilder.append("/");
                rootBuilder.append(RootCommand);
            }
            rootBuilder.append(" ");
            rootBuilder.append(pre);
        }

        boolean first = true;

        if(subCommands.isEmpty())
            builder.append("N/A");
        else {
            for(String sub : subCommands) {
                if(!first)
                    builder.append(delimiter);
                if(prefixRoot)
                    builder.append(rootBuilder.toString());
                if(prefixRoot || !first)
                    builder.append(" ");
                builder.append(sub);
                if(first) first = false;
            }
        }

        return builder.toString();
    }

    public static String getAllCommands() {
        StringBuilder builder = new StringBuilder(200);
        builder.append("Available Commands: ");
        List<String> commands = new LinkedList<String>();
        commands.add("help");
        commands.add("list (Gives a list of signs)");
        commands.add("sign SIGN (Replace SIGN with a type of sign)");
        commands.add("reload (Reloads the signshop configs)");
        commands.add("[about|version] (Gives version information about signshop)");
        for(String comm : commands) {
            builder.append("\n/");
            builder.append(RootCommand);
            builder.append(" ");
            builder.append(comm);
        }
        builder.append("\n");
        return builder.toString();
    }

}

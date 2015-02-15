
package org.wargamer2010.signshop.commands;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.commandUtil;
import org.wargamer2010.signshop.util.signshopUtil;

public class HelpHandler implements ICommandHandler {
    private static ICommandHandler instance = new HelpHandler();

    private HelpHandler() {

    }

    public static ICommandHandler getInstance() {
        return instance;
    }

    public Collection<String> getFilteredOperation(SignShopPlayer player) {
        Collection<String> all = SignShopConfig.getOperations();
        Collection<String> filtered = new LinkedList<String>();
        Collection<String> aliased = new LinkedList<String>();

        if(player == null)
            filtered.addAll(all);
        else {
            for(String op : all) {
                List<String> operation = SignShopConfig.getBlocks(op);
                if(!operation.contains("playerIsOp") && (player.hasPerm(("SignShop.Signs." + op), false) || player.hasPerm(("SignShop.Signs.*"), false)))
                    filtered.add(op);
                else if(operation.contains("playerIsOp") && (player.hasPerm(("SignShop.Admin."+op), true) || player.hasPerm(("SignShop.Admin.*"), true)))
                    filtered.add(op);
            }
        }

        for(String op : filtered) {
            Collection<String> temp = SignShopConfig.getAliases(op);
            if(temp.isEmpty())
                aliased.add(op);
            else
                aliased.addAll(temp);
        }
        
        return aliased;
    }

    @Override
    public boolean handle(String command, String[] args, SignShopPlayer player) {
        StringBuilder messageBuilder = new StringBuilder(200);
        if(!command.equals("help") && !command.equals("sign") && !command.isEmpty() && !command.equals("list"))
            return false;

        if(args.length == 0 && (command.equals("help") || command.isEmpty()))
            messageBuilder.append(commandUtil.getAllCommands());

        String availableSigns = "Available Signs: ";
        String moreInfo = "\n\n(For more information about a sign, type /signshop sign SIGN, where SIGN is the sign's name from this list)";
        String signList = commandUtil.getCommandList(getFilteredOperation(player), availableSigns, ",", false, "sign");

        if(command.equals("list")) {
            messageBuilder.append(signList);
            messageBuilder.append(moreInfo);
        } else if(args.length == 0 && command.equals("sign")) {
            messageBuilder.append(signList);
            messageBuilder.append(moreInfo);
        } else if(!command.isEmpty() && args.length > 0 && command.equals("sign")) {
            Map<String, String> messageParts = new LinkedHashMap<String, String>();
            messageParts.put("!linkmaterial", signshopUtil.capFirstLetter(SignShopConfig.getLinkMaterial().name().toLowerCase()));

            String temp = SignShopConfig.getMessage("help", args[0], messageParts).replace(". ", ".\n- ");
            if(temp.trim().isEmpty()) {
                messageBuilder.append("Sign help does not exist.\n");
                messageBuilder.append(signList);
            } else {
                messageBuilder.append("*** ");
                messageBuilder.append(args[0]);
                messageBuilder.append(" *** \n");
                messageBuilder.append("-  ");
                messageBuilder.append(temp);
            }
        } else {
            messageBuilder = new StringBuilder(200);
            messageBuilder.append(commandUtil.getAllCommands());
        }

        commandUtil.sendToPlayerOrConsole(messageBuilder.toString(), player);
        return true;
    }

}

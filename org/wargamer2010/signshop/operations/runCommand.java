package org.wargamer2010.signshop.operations;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class runCommand implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        SignShopPlayer ssPlayer = ssArgs.get_ssPlayer();
        String sOperation = ssArgs.get_sOperation();
        Boolean hasStartPerm = false;

        ssArgs.setMessagePart("!customer", ssPlayer.getName());
        ssArgs.setMessagePart("!owner", ssArgs.get_ssOwner().getName());
        ssArgs.setMessagePart("!player", ssPlayer.getName());
        ssArgs.setMessagePart("!world", ssPlayer.getPlayer().getWorld().getName());
        String[] sLines = ((Sign) ssArgs.get_bSign().getState()).getLines();
        for(int i = 0; i < sLines.length; i++)
            ssArgs.setMessagePart(("!line" + (i+1)), (sLines[i] == null ? "" : sLines[i]));
        boolean isOK = true;

        if(SignShopConfig.Commands.containsKey(sOperation.toLowerCase())) {
            List<String> commands = SignShopConfig.Commands.get(sOperation.toLowerCase());
            for(String sCommand : commands) {
                boolean ok = true;
                if(sCommand != null && sCommand.length() > 0) {
                    sCommand = SignShopConfig.fillInBlanks(sCommand, ssArgs.messageParts);
                    sCommand = SignShopConfig.fillInBlanks(sCommand, ssArgs.messageParts);
                    if(ssArgs.isOperationParameter("asOriginalUser")) {
                        ok = Bukkit.getServer().dispatchCommand(ssPlayer.getPlayer(), sCommand);
                    } else if(ssArgs.isOperationParameter("asUser")) {
                        if(!ssPlayer.hasPerm("*", true))
                            Vault.permission.playerAdd(ssPlayer.getPlayer(), "*");
                        else
                            hasStartPerm = true;
                        ok = Bukkit.getServer().dispatchCommand(ssPlayer.getPlayer(), sCommand);
                        if(!hasStartPerm)
                            Vault.permission.playerRemove(ssPlayer.getPlayer(), "*");
                    } else {
                        ok = Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), sCommand);
                    }
                }
                isOK = (ok ? isOK : false);
            }
        }
        return isOK;
    }
}

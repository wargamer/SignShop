package org.wargamer2010.signshop.operations;

import java.util.List;
import org.bukkit.Bukkit;
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
        SignShopPlayer ssPlayer = ssArgs.getPlayer().get();
        String sOperation = ssArgs.getOperation().get();
        Boolean hasStartPerm = false;

        boolean isOK = true;

        if(SignShopConfig.getCommands().containsKey(sOperation.toLowerCase())) {
            List<String> commands = SignShopConfig.getCommands().get(sOperation.toLowerCase());
            for(String command : commands) {
                boolean ok = true;
                String sCommand = command;
                if(sCommand != null && sCommand.length() > 0) {
                    sCommand = SignShopConfig.fillInBlanks(sCommand, ssArgs.getMessageParts());
                    sCommand = SignShopConfig.fillInBlanks(sCommand, ssArgs.getMessageParts());
                    if(ssArgs.isOperationParameter("asOriginalUser")) {
                        ok = Bukkit.getServer().dispatchCommand(ssPlayer.getPlayer(), sCommand);
                    } else if(ssArgs.isOperationParameter("asUser")) {
                        if(!ssPlayer.hasPerm("*", true))
                            Vault.getPermission().playerAdd(ssPlayer.getPlayer(), "*");
                        else
                            hasStartPerm = true;
                        ok = Bukkit.getServer().dispatchCommand(ssPlayer.getPlayer(), sCommand);
                        if(!hasStartPerm)
                            Vault.getPermission().playerRemove(ssPlayer.getPlayer(), "*");
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

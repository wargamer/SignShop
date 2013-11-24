package org.wargamer2010.signshop.operations;

import java.util.logging.Level;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.timing.TimedCommand;

public class runTimedCommand implements SignShopOperation {
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
        if(SignShopConfig.getDelayedCommands().containsKey(ssArgs.getOperation().get().toLowerCase())) {
            String commandType;
            if(ssArgs.isOperationParameter("asOriginalUser"))
                commandType = "asOriginalUser";
            else if(ssArgs.isOperationParameter("asUser"))
                commandType = "asUser";
            else
                commandType = "asConsole";

            TimedCommand cmd = new TimedCommand(ssArgs.getOperation().get(), commandType, ssArgs.getMessageParts());
            try {
                Integer delay = Integer.parseInt(ssArgs.getFirstOperationParameter());
                SignShop.getTimeManager().addExpirable(cmd, delay);
                return true;
            } catch(NumberFormatException ex) {
                ssArgs.getPlayer().get().sendMessage("Found invalid configuration for this shop, please contact the server administrator.");
                SignShop.log("Found invalid delay for runTimedCommand for shop type: " + ssArgs.getOperation().get(), Level.WARNING);
                return false;
            }
        }

        // No commands to run
        return true;
    }
}

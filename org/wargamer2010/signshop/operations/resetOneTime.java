package org.wargamer2010.signshop.operations;

import java.util.logging.Level;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class resetOneTime implements SignShopOperation {

    private String getParam(SignShopArguments ssArgs) {
        String rawparam = ssArgs.getOperation().get().toLowerCase();
        if(ssArgs.hasOperationParameters())
            rawparam = ssArgs.getFirstOperationParameter().toLowerCase();
        rawparam = SignShopConfig.fillInBlanks(rawparam, ssArgs.getMessageParts());
        rawparam = SignShopConfig.fillInBlanks(rawparam, ssArgs.getMessageParts());
        return rawparam;
    }

    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(!ssArgs.hasOperationParameters()) {
            ssArgs.getPlayer().get().sendMessage("Config error, please check the logs for more information.");
            SignShop.log("Missing parameter for resetOneTime, please check the config.yml and Quick Reference.", Level.WARNING);
            return false;
        }

        String param = getParam(ssArgs);
        ssArgs.setMessagePart("!param", param);
        SignShopPlayer ssPlayer = ssArgs.getPlayer().get();
        if(ssPlayer == null)
            return true;
        if(!ssPlayer.hasMeta(param)) {
            ssArgs.sendFailedRequirementsMessage("nothing_to_reset_ontime");
            return false;
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        String param = getParam(ssArgs);
        ssArgs.setMessagePart("!param", param);
        SignShopPlayer ssPlayer = ssArgs.getPlayer().get();
        boolean ok = ssPlayer.removeMeta(param);
        if(!ok)
            ssPlayer.sendMessage("Could not reset the Metadata needed for this shop. Please check the logs for more information.");
        return ok;
    }
}

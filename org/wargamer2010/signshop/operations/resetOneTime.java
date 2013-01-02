package org.wargamer2010.signshop.operations;

import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class resetOneTime implements SignShopOperation {

    private String getParam(SignShopArguments ssArgs) {
        String rawparam = ssArgs.get_sOperation().toLowerCase();
        if(ssArgs.hasOperationParameters())
            rawparam = ssArgs.getFirstOperationParameter().toLowerCase();
        rawparam = SignShopConfig.fillInBlanks(rawparam, ssArgs.messageParts);
        rawparam = SignShopConfig.fillInBlanks(rawparam, ssArgs.messageParts);
        return rawparam;
    }

    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(!ssArgs.hasOperationParameters()) {
            ssArgs.get_ssPlayer().sendMessage("Config error, please check the logs for more information.");
            SignShop.log("Missing parameter for resetOneTime, please check the config.yml and Quick Reference.", Level.WARNING);
            return false;
        }

        String param = getParam(ssArgs);
        ssArgs.messageParts.put("!param", param);
        SignShopPlayer ssPlayer = ssArgs.get_ssPlayer();
        if(ssPlayer == null)
            return true;
        if(!ssPlayer.hasMeta(param)) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("nothing_to_reset_ontime", ssArgs.messageParts));
            return false;
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        String param = getParam(ssArgs);
        ssArgs.messageParts.put("!param", param);
        SignShopPlayer ssPlayer = ssArgs.get_ssPlayer();
        boolean ok = ssPlayer.removeMeta(param);
        if(!ok)
            ssPlayer.sendMessage("Could not reset the Metadata needed for this shop. Please check the logs for more information.");
        return ok;
    }
}

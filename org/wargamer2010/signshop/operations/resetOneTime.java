package org.wargamer2010.signshop.operations;

import java.util.logging.Level;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.signshopUtil;

public class resetOneTime implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        signshopUtil.getParam(ssArgs);
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(!ssArgs.hasOperationParameters()) {
            ssArgs.getPlayer().get().sendMessage("Config error, please check the logs for more information.");
            SignShop.log("Missing parameter for resetOneTime, please check the config.yml and Quick Reference.", Level.WARNING);
            return false;
        }
        if(!ssArgs.isPlayerOnline())
            return true;

        String param = signshopUtil.getParam(ssArgs);
        SignShopPlayer ssPlayer = ssArgs.getPlayer().get();
        if(!ssPlayer.hasMeta(param)) {
            ssArgs.sendFailedRequirementsMessage("nothing_to_reset_ontime");
            return false;
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        String param = signshopUtil.getParam(ssArgs);
        SignShopPlayer ssPlayer = ssArgs.getPlayer().get();
        boolean ok = ssPlayer.removeMeta(param);
        if(!ok)
            ssPlayer.sendMessage("Could not reset the Metadata needed for this shop. Please check the logs for more information.");
        return ok;
    }
}

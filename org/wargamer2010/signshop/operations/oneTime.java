package org.wargamer2010.signshop.operations;

import java.util.Date;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.signshopUtil;

public class oneTime implements SignShopOperation {

    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        signshopUtil.getParam(ssArgs);
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        String param = signshopUtil.getParam(ssArgs);
        SignShopPlayer ssPlayer = ssArgs.getPlayer().get();
        if(!ssArgs.isPlayerOnline())
            return true;
        if(ssPlayer.hasMeta(param)) {
            ssArgs.sendFailedRequirementsMessage("only_one_time");
            return false;
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        String param = signshopUtil.getParam(ssArgs);
        SignShopPlayer ssPlayer = ssArgs.getPlayer().get();

        boolean ok = ssPlayer.setMeta(param, Long.toString(new Date().getTime()));
        if(!ok)
            ssPlayer.sendMessage("Could not set the Metadata needed for this shop. Please check the logs for more information.");
        return ok;
    }
}

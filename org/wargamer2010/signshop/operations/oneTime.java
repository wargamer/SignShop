package org.wargamer2010.signshop.operations;

import java.util.Date;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class oneTime implements SignShopOperation {

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
        String param = getParam(ssArgs);
        SignShopPlayer ssPlayer = ssArgs.getPlayer().get();
        if(ssPlayer == null)
            return true;
        if(ssPlayer.hasMeta(param)) {
            ssArgs.sendFailedRequirementsMessage("only_one_time");
            return false;
        }
        ssArgs.setMessagePart("!param", param);
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        String param = getParam(ssArgs);
        SignShopPlayer ssPlayer = ssArgs.getPlayer().get();

        boolean ok = ssPlayer.setMeta(param, Long.toString(new Date().getTime()));
        if(!ok)
            ssPlayer.sendMessage("Could not set the Metadata needed for this shop. Please check the logs for more information.");
        ssArgs.setMessagePart("!param", param);
        return ok;
    }
}

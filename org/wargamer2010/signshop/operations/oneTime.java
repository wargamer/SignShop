package org.wargamer2010.signshop.operations;

import java.util.Date;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class oneTime implements SignShopOperation {

    private String getParam(SignShopArguments ssArgs) {
        String rawparam = ssArgs.get_sOperation().toLowerCase();
        if(ssArgs.hasOperationParameters())
            rawparam = ssArgs.getFirstOperationParameter().toLowerCase();
        return rawparam;
    }

    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        String param = getParam(ssArgs);
        SignShopPlayer ssPlayer = ssArgs.get_ssPlayer();
        if(ssPlayer == null)
            return true;
        if(ssPlayer.hasMeta(param)) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("only_one_time", ssArgs.messageParts));
            return false;
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        String param = getParam(ssArgs);
        SignShopPlayer ssPlayer = ssArgs.get_ssPlayer();

        boolean ok = ssPlayer.setMeta(param, Long.toString(new Date().getTime()));
        if(!ok)
            ssPlayer.sendMessage("Could not set the Metadata needed for this shop. Please check the logs for more information.");
        return ok;
    }
}

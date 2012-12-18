package org.wargamer2010.signshop.operations;

import java.util.Date;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;

public class oneTime implements SignShopOperation {

    private String getParam(SignShopArguments ssArgs) {
        String rawparam = ssArgs.get_sOperation().toLowerCase();
        if(ssArgs.hasOperationParameters())
            rawparam = ssArgs.getFirstOperationParameter();
        return ("signshop_" + rawparam);
    }

    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        String param = getParam(ssArgs);
        Player player = ssArgs.get_ssPlayer().getPlayer();
        if(player == null)
            return true;
        if(player.getMetadata(param) != null && !player.getMetadata(param).isEmpty()) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("only_one_time", ssArgs.messageParts));
            return false;
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        String param = getParam(ssArgs);
        Player player = ssArgs.get_ssPlayer().getPlayer();

        player.setMetadata(param, new FixedMetadataValue(SignShop.getInstance(), (new Date().getTime())));

        return true;
    }
}

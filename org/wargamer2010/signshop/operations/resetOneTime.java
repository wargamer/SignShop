package org.wargamer2010.signshop.operations;

import java.util.Date;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;

public class resetOneTime implements SignShopOperation {

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
        if(!ssArgs.hasOperationParameters()) {
            ssArgs.get_ssPlayer().sendMessage("Config error, please check the logs for more information.");
            SignShop.log("Missing parameter for resetOneTime, please check the config.yml and Quick Reference.", Level.WARNING);
            return false;
        }

        String param = getParam(ssArgs);
        Player player = ssArgs.get_ssPlayer().getPlayer();
        if(player == null)
            return true;
        if(player.getMetadata(param) == null || player.getMetadata(param).isEmpty()) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("nothing_to_reset_ontime", ssArgs.messageParts));
            return false;
        }

        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        String param = ("signshop_" + ssArgs.getFirstOperationParameter());
        Player player = ssArgs.get_ssPlayer().getPlayer();
        player.removeMetadata(param, SignShop.getInstance());

        return true;
    }
}

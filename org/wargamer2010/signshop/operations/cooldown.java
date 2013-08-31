package org.wargamer2010.signshop.operations;

import java.util.Date;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.signshopUtil;

public class cooldown implements SignShopOperation {

    public long getCooldown(SignShopArguments ssArgs) {
        if(ssArgs.hasOperationParameters()) {
            try {
                long cooldown = Long.parseLong(ssArgs.getFirstOperationParameter().toLowerCase());
                if(cooldown > 0)
                    return cooldown;
            } catch(NumberFormatException ex) { }
        }
        ssArgs.getPlayer().get().sendMessage("Please specify a valid amount for the cooldown block. Check your config.yml.");
        return -1;
    }

    private boolean setMeta(SignShopPlayer player, String key) {
        boolean ok = player.setMeta(key, Long.toString(new Date().getTime()));
        if(!ok)
            player.sendMessage("Could not set the Metadata needed for this shop. Please check the logs for more information.");
        return ok;
    }

    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return (getCooldown(ssArgs) > 0);
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        String param = ("cooldown_" + ssArgs.getOperation().get());
        SignShopPlayer ssPlayer = ssArgs.getPlayer().get();
        ssArgs.setMessagePart("!param", param);

        if(ssPlayer == null)
            return true;

        if(!ssPlayer.hasMeta(param))
            return true;

        try {
            long currentTime = new Date().getTime();
            long cooldown = getCooldown(ssArgs);
            if(cooldown == -1)
                return false;
            long lastUse = Long.parseLong(ssPlayer.getMeta(param));
            long diffInSeconds = ((currentTime - lastUse) / 1000);

            if(diffInSeconds < cooldown) {
                long left = (cooldown - diffInSeconds);
                if(left >= 1)
                    ssArgs.setMessagePart("!cooldownleft", Long.toString(left));
                else
                    ssArgs.setMessagePart("!cooldownleft", "< 1");

                ssPlayer.sendMessage(SignShopConfig.getError("shop_on_cooldown", ssArgs.getMessageParts()));
                return false;
            }
        } catch(NumberFormatException ex) {
            ssPlayer.sendMessage("Invalid cooldown Metadata found. Please check the logs for more information.");
            return false;
        }

        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        String param = ("cooldown_" + ssArgs.getOperation().get());
        boolean ok = setMeta(ssArgs.getPlayer().get(), param);
        ssArgs.setMessagePart("!param", param);
        return ok;
    }
}

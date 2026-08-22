package org.wargamer2010.signshop.operations;

import org.bukkit.World;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.player.SignShopPlayer;

/**
 * Shop operation that sets the weather to rain with thunder.
 */
public class setRaining implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        World world = ssArgs.getPlayer().get().getPlayer().getWorld();
        if(world.hasStorm() && world.isThundering()) {
            ssArgs.sendFailedRequirementsMessage("already_raining");
            return false;
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        World world = ssArgs.getPlayer().get().getPlayer().getWorld();
        world.setStorm(true);
        world.setThundering(true);

        SignShopPlayer.broadcastMsg(world, SignShop.getInstance().getSignShopConfig().getError("made_rain", ssArgs.getMessageParts()));
        return true;
    }
}

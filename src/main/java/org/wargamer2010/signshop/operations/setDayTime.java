package org.wargamer2010.signshop.operations;

import org.bukkit.World;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.player.SignShopPlayer;

/**
 * Shop operation that sets the world time to day (time 0).
 */
public class setDayTime implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {        
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        World world = ssArgs.getPlayer().get().getPlayer().getWorld();
        world.setTime(0);
        SignShopPlayer.broadcastMsg(world, SignShop.getInstance().getSignShopConfig().getError("made_day", ssArgs.getMessageParts()));
        return true;
    }
}

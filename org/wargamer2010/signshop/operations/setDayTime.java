package org.wargamer2010.signshop.operations;

import org.bukkit.World;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.SignShop;

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
        World world = ssArgs.get_ssPlayer().getPlayer().getWorld();
        world.setTime(0);        
        SignShopPlayer.broadcastMsg(world, SignShop.Errors.get("made_day").replace("!player", ssArgs.get_ssPlayer().getPlayer().getDisplayName()));
        return true;
    }
}

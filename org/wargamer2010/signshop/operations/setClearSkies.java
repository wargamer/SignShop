package org.wargamer2010.signshop.operations;

import org.bukkit.World;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.SignShop;

public class setClearSkies implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        World world = ssArgs.ssPlayer.getPlayer().getWorld();
        if(!world.hasStorm() && !world.isThundering()) {
            ssArgs.ssPlayer.sendMessage(SignShop.Errors.get("already_clear_skies"));
            return false;
        }
        return true;        
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {        
        World world = ssArgs.ssPlayer.getPlayer().getWorld();
        world.setStorm(false);
        world.setThundering(false);

        SignShopPlayer.broadcastMsg(world,SignShop.Errors.get("made_clear_skies").replace("!player",ssArgs.ssPlayer.getPlayer().getDisplayName()));
        return true;
    }
}

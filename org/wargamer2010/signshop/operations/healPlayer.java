package org.wargamer2010.signshop.operations;

import org.wargamer2010.signshop.configuration.SignShopConfig;

public class healPlayer implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(ssArgs.getPlayer().get().getPlayer() == null)
            return true;
        if(ssArgs.getPlayer().get().getPlayer().getHealth() >= 20) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("already_full_health", ssArgs.getMessageParts()));
            return false;
        }
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ssArgs.getPlayer().get().getPlayer().setHealth(20);
        return true;
    }
}

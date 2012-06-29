package org.wargamer2010.signshop.operations;

import org.wargamer2010.signshop.SignShop;

public class healPlayer implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(ssArgs.get_ssPlayer().getPlayer() == null)
            return true;
        if(ssArgs.get_ssPlayer().getPlayer().getHealth() >= 20) {
            ssArgs.get_ssPlayer().sendMessage(SignShop.Errors.get("already_full_health"));
            return false;
        }
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ssArgs.get_ssPlayer().getPlayer().setHealth(20);
        return true;
    }
}

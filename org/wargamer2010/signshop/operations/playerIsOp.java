package org.wargamer2010.signshop.operations;

import org.wargamer2010.signshop.SignShop;

public class playerIsOp implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(!ssArgs.get_ssPlayer().hasPerm(("SignShop.Admin."+ssArgs.get_sOperation()), true)) {
            ssArgs.get_ssPlayer().sendMessage(SignShop.Errors.get("no_permission"));
            return false;        
        }
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {        
        return true;
    }
}

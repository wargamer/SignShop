package org.wargamer2010.signshop.operations;

import org.wargamer2010.signshop.configuration.SignShopConfig;

public class playerIsOp implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {        
        if(!ssArgs.get_ssPlayer().hasPerm(("SignShop.Admin."+ssArgs.get_sOperation()), true) && !ssArgs.get_ssPlayer().hasPerm(("SignShop.Admin.*"), true)) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("no_permission", ssArgs.messageParts));
            return false;        
        }
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {        
        return true;
    }
}

package org.wargamer2010.signshop.operations;

import org.wargamer2010.signshop.configuration.SignShopConfig;

public class playerIsOp implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {        
        if(!ssArgs.getPlayer().get().hasPerm(("SignShop.Admin."+ssArgs.getOperation().get()), true) && !ssArgs.getPlayer().get().hasPerm(("SignShop.Admin.*"), true)) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("no_permission", ssArgs.getMessageParts()));
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

package org.wargamer2010.signshop.operations;

public class takePlayerInventory implements SignShopOperation {    
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
        ssArgs.ssPlayer.getPlayer().getInventory().clear();        
        return true;
    }
}

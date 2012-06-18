package org.wargamer2010.signshop.operations;

public interface SignShopOperation {
    public Boolean setupOperation(SignShopArguments ssArgs);
    
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck);
    
    public Boolean runOperation(SignShopArguments ssArgs);
}

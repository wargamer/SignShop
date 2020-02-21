package org.wargamer2010.signshop.operations;

public interface SignShopOperation {
    Boolean setupOperation(SignShopArguments ssArgs);

    Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck);

    Boolean runOperation(SignShopArguments ssArgs);
}

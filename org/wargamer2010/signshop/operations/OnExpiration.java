
package org.wargamer2010.signshop.operations;

import java.util.logging.Level;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.events.SSEvent;
import org.wargamer2010.signshop.events.SSExpiredEvent;
import org.wargamer2010.signshop.util.signshopUtil;

public class OnExpiration extends SignShopEventHandler {

    private boolean notifyUserOfInvalidParam(String block) {
        SignShop.log("An invalid parameter has been specified for the OnHotelExpire block: " + block, Level.WARNING);
        return false;
    }

    private SignShopOperation getBlockToCall(SignShopArguments ssArgs) {
        if(!ssArgs.hasOperationParameters()) {
            notifyUserOfInvalidParam("");
            return null;
        }

        SignShopOperation blockToCall = signshopUtil.getSignShopBlock(ssArgs.getFirstOperationParameter());
        if(blockToCall == null) {
            notifyUserOfInvalidParam(ssArgs.getFirstOperationParameter());
            return null;
        }

        return blockToCall;
    }

    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        SignShopOperation blockToCall = getBlockToCall(ssArgs);
        if(blockToCall == null)
            return false;
        return blockToCall.setupOperation(ssArgs);
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        SignShopOperation blockToCall = getBlockToCall(ssArgs);
        if(blockToCall == null)
            return false;
        return blockToCall.checkRequirements(ssArgs, activeCheck);
    }

    @Override
    public boolean handleEvent(SignShopArguments ssArgs, SSEvent event) {
        if(!(event instanceof SSExpiredEvent))
            return true; // No error
        SignShopOperation blockToCall = getBlockToCall(ssArgs);
        if(blockToCall == null)
            return false;

        if(!(blockToCall.checkRequirements(ssArgs, true)))
            return false;
        if(!(blockToCall.runOperation(ssArgs)))
            return false;
        return true;
    }
}

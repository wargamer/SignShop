
package org.wargamer2010.signshop.operations;

import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.events.SSEvent;
import org.wargamer2010.signshop.events.SSExpiredEvent;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Event handler that executes a specified operation when a timed shop expires.
 */
public class OnExpiration extends SignShopEventHandler {

    private void notifyUserOfInvalidParam(String block) {
        SignShop.log("An invalid parameter has been specified for the OnHotelExpire block: " + block, Level.WARNING);
    }

    private SignShopOperationListItem getBlockToCall(SignShopArguments ssArgs) {
        if(!ssArgs.hasOperationParameters()) {
            notifyUserOfInvalidParam("");
            return null;
        }

        List<SignShopOperationListItem> blocks = signshopUtil.getSignShopOps(
                Collections.singletonList(ssArgs.getFirstOperationParameter())
        );

        if(blocks == null || blocks.isEmpty()) {
            notifyUserOfInvalidParam(ssArgs.getFirstOperationParameter());
            return null;
        }

        return blocks.getFirst();
    }

    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        SignShopOperationListItem blockToCall = getBlockToCall(ssArgs);
        if(blockToCall == null)
            return false;
        ssArgs.setOperationParameters(blockToCall.getParameters());
        return blockToCall.getOperation().setupOperation(ssArgs);
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        SignShopOperationListItem blockToCall = getBlockToCall(ssArgs);
        if(blockToCall == null)
            return false;
        ssArgs.setOperationParameters(blockToCall.getParameters());
        return blockToCall.getOperation().checkRequirements(ssArgs, activeCheck);
    }

    @Override
    public boolean handleEvent(SignShopArguments ssArgs, SSEvent event) {
        if(!(event instanceof SSExpiredEvent))
            return true; // No error
        SignShopOperationListItem blockToCall = getBlockToCall(ssArgs);
        if(blockToCall == null)
            return false;

        SignShopOperation op = blockToCall.getOperation();
        ssArgs.setOperationParameters(blockToCall.getParameters());

        if(!(op.checkRequirements(ssArgs, true)))
            return false;
        return op.runOperation(ssArgs);
    }
}

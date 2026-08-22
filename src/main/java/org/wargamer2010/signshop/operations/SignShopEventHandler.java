
package org.wargamer2010.signshop.operations;

import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.events.SSEvent;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.List;
import java.util.logging.Level;

/**
 * Abstract base for operations that respond to SignShop internal events.
 */
public abstract class SignShopEventHandler implements SignShopOperation {
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        return true;
    }

    public abstract boolean handleEvent(SignShopArguments ssArgs, SSEvent event);

    public static boolean dispatchEvent(SignShopArguments ssArgs, SSEvent event, String operation) {
        List<String> stringops = SignShop.getInstance().getSignShopConfig().getBlocks(operation);
        if(stringops.isEmpty()) {
            SignShop.log("Invalid operation found while trying to dispatch event: " + operation, Level.WARNING);
            return false;
        }

        List<SignShopOperationListItem> opitems = signshopUtil.getSignShopOps(stringops);
        for(SignShopOperationListItem item : opitems) {
            if(item.getOperation() instanceof SignShopEventHandler) {
                ssArgs.setOperationParameters(item.getParameters());
                SignShopEventHandler handler = (SignShopEventHandler) item.getOperation();
                boolean result = handler.handleEvent(ssArgs, event);
                if(!result)
                    return false;
            }
        }

        return true;
    }
}

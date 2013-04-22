package org.wargamer2010.signshop.operations;

import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.events.SSEventFactory;
import org.wargamer2010.signshop.events.SSMoneyEventType;
import org.wargamer2010.signshop.events.SSMoneyTransactionEvent;
import org.wargamer2010.signshop.util.economyUtil;

public class takePlayerMoney implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        ssArgs.setMessagePart("!price", economyUtil.formatMoney(ssArgs.getPrice().get()));
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        SSMoneyTransactionEvent event = SSEventFactory.generateMoneyEvent(ssArgs, ssArgs.getPrice().get(), SSMoneyEventType.TakeFromPlayer, true);
        SignShop.scheduleEvent(event);
        ssArgs.getPrice().set(event.getAmount());
        return (!event.isCancelled() && event.isHandled());
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        SSMoneyTransactionEvent event = SSEventFactory.generateMoneyEvent(ssArgs, ssArgs.getPrice().get(), SSMoneyEventType.TakeFromPlayer, false);
        SignShop.scheduleEvent(event);
        ssArgs.getPrice().set(event.getAmount());
        return (!event.isCancelled() && event.isHandled());
    }
}

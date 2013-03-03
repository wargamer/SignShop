package org.wargamer2010.signshop.operations;

import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.events.SSEventFactory;
import org.wargamer2010.signshop.events.SSMoneyEventType;
import org.wargamer2010.signshop.events.SSMoneyTransactionEvent;
import org.wargamer2010.signshop.util.economyUtil;

public class givePlayerMoney implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        ssArgs.setMessagePart("!price", economyUtil.formatMoney(ssArgs.get_fPrice()));
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        SSMoneyTransactionEvent event = SSEventFactory.generateMoneyEvent(ssArgs, ssArgs.get_fPrice(), SSMoneyEventType.GiveToPlayer, true);
        SignShop.scheduleEvent(event);
        return (!event.isCancelled() && event.isHandled());
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        SSMoneyTransactionEvent event = SSEventFactory.generateMoneyEvent(ssArgs, ssArgs.get_fPrice(), SSMoneyEventType.GiveToPlayer, false);
        SignShop.scheduleEvent(event);
        return (!event.isCancelled() && event.isHandled());
    }
}

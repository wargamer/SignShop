
package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.events.SSMoneyTransactionEvent;
import org.wargamer2010.signshop.money.MoneyModifierManager;
import org.wargamer2010.signshop.util.economyUtil;

public class MoneyModifierListener implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onSSMoneyTransactionEvent(SSMoneyTransactionEvent event) {
        if(event.isCancelled())
            return;
        double returnValue;

        if(event.getArguments() == null)
            returnValue = MoneyModifierManager.applyModifiers(event.getPlayer(), event.getPrice(), event.getOperation(), event.getTransactionType());
        else {
            event.getArguments().getPrice().set(event.getPrice());
            returnValue = MoneyModifierManager.applyModifiers(event.getArguments(), event.getTransactionType());
        }

        event.setPrice(returnValue);
        event.setMessagePart("!price", economyUtil.formatMoney(returnValue));
    }
}

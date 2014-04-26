package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import static org.wargamer2010.signshop.events.SSMoneyEventType.GiveToOwner;
import static org.wargamer2010.signshop.events.SSMoneyEventType.GiveToPlayer;
import static org.wargamer2010.signshop.events.SSMoneyEventType.TakeFromOwner;
import static org.wargamer2010.signshop.events.SSMoneyEventType.TakeFromPlayer;
import static org.wargamer2010.signshop.events.SSMoneyEventType.Unknown;
import org.wargamer2010.signshop.events.SSMoneyTransactionEvent;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class DefaultMoneyTransaction implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSSMoneyTransaction(SSMoneyTransactionEvent event) {
        if(event.isHandled() || event.isCancelled())
            return;
        if(event.getPlayer().getPlayer() == null) {
            // Make ShopUpdater happy
            event.setHandled(true);
            return;
        }

        SignShopPlayer ssOwner = event.getShop().getOwner();
        if(event.isCheckOnly()) {
            switch(event.getTransactionType()) {
                case GiveToOwner:
                    if(!ssOwner.canHaveMoney(event.getAmount())) {
                        event.sendFailedRequirementsMessage("overstocked");
                        event.setCancelled(true);
                    }
                break;
                case TakeFromOwner:
                    if(!ssOwner.hasMoney(event.getAmount())) {
                        event.sendFailedRequirementsMessage("no_shop_money");
                        event.setCancelled(true);
                    }
                break;
                case GiveToPlayer:
                    if(!event.getPlayer().canHaveMoney(event.getAmount())) {
                        event.sendFailedRequirementsMessage("player_overstocked");
                        event.setCancelled(true);
                    }
                break;
                case TakeFromPlayer:
                    if(!event.getPlayer().hasMoney(event.getAmount())) {
                        event.sendFailedRequirementsMessage("no_player_money");
                        event.setCancelled(true);
                    }
                break;
                case Unknown:
                    return;
            }
        } else {
            boolean bTransaction = false;

            switch(event.getTransactionType()) {
                case GiveToOwner:
                    bTransaction = ssOwner.mutateMoney(event.getAmount());
                break;
                case TakeFromOwner:
                    bTransaction = ssOwner.mutateMoney(-event.getAmount());
                break;
                case GiveToPlayer:
                    bTransaction = event.getPlayer().mutateMoney(event.getAmount());
                break;
                case TakeFromPlayer:
                    bTransaction = event.getPlayer().mutateMoney(-event.getAmount());
                break;
                case Unknown:
                    return;
            }

            if(!bTransaction) {
                event.getPlayer().sendMessage("The money transaction failed, please contact the System Administrator");
                event.setCancelled(true);
            }
        }

        event.setHandled(true);
    }
}

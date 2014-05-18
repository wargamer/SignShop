package org.wargamer2010.signshop.listeners.sslisteners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.Seller;
import static org.wargamer2010.signshop.events.SSMoneyEventType.GiveToOwner;
import static org.wargamer2010.signshop.events.SSMoneyEventType.GiveToPlayer;
import static org.wargamer2010.signshop.events.SSMoneyEventType.TakeFromOwner;
import static org.wargamer2010.signshop.events.SSMoneyEventType.TakeFromPlayer;
import static org.wargamer2010.signshop.events.SSMoneyEventType.Unknown;
import org.wargamer2010.signshop.events.SSMoneyRequestType;
import org.wargamer2010.signshop.events.SSMoneyTransactionEvent;
import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;
import static org.wargamer2010.signshop.util.signshopUtil.getSignsFromMisc;
import static org.wargamer2010.signshop.util.signshopUtil.lineIsEmpty;

public class SharedMoneyTransaction implements Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    public void onSSMoneyTransaction(SSMoneyTransactionEvent event) {
        if(event.isHandled() || event.isCancelled() || event.getPlayer().getPlayer() == null)
            return;
        if(event.getShop() == null || !event.getShop().hasMisc("sharesigns"))
            return;
        if(!event.isBalanceOrExecution())
            return;

        if(event.getRequestType() == SSMoneyRequestType.CheckBalance) {
            switch(event.getTransactionType()) {
                case GiveToOwner:
                    // This is tricky, we'd have to check whether all the people we're sharing with could
                    // take their share of the money
                break;
                case TakeFromOwner:
                    return;
                case GiveToPlayer:
                    return;
                case TakeFromPlayer:
                    return;
                case Unknown:
                    return;
            }
        } else {
            boolean bTransaction = false;

            switch(event.getTransactionType()) {
                case GiveToOwner:
                    bTransaction = distributeMoney(event.getShop(), event.getPrice(), event.getPlayer());
                break;
                case TakeFromOwner:
                    return;
                case GiveToPlayer:
                    return;
                case TakeFromPlayer:
                    return;
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

    private static boolean distributeMoney(Seller seller, double fPrice, SignShopPlayer ssPlayer) {
        List<Block> shareSigns = getSignsFromMisc(seller, "sharesigns");
        SignShopPlayer ssOwner = seller.getOwner();
        if(shareSigns.isEmpty()) {
            return ssOwner.mutateMoney(fPrice);
        } else {
            Boolean bTotalTransaction;
            Map<String, Integer> shares = new HashMap<String, Integer>();
            for(Block sharesign : shareSigns) {
                if(itemUtil.clickedSign(sharesign)) {
                    shares.putAll(getShares((Sign)sharesign.getState(), ssPlayer));
                }
            }
            Integer totalPercentage = 0;
            for(Map.Entry<String, Integer> share : shares.entrySet()) {

                double amount = (fPrice / 100 * share.getValue());
                SignShopPlayer sharee = PlayerIdentifier.getByName(share.getKey());
                if(sharee == null || !sharee.playerExistsOnServer())
                    ssOwner.sendMessage("Not giving " + share.getKey() + " " + economyUtil.formatMoney(amount) + " because player doesn't exist!");
                else {
                    ssOwner.sendMessage("Giving " + share.getKey() + " a share of " + economyUtil.formatMoney(amount));
                    sharee.sendMessage("You were given a share of " + economyUtil.formatMoney(amount));
                    totalPercentage += share.getValue();
                    bTotalTransaction = sharee.mutateMoney(amount);
                    if(!bTotalTransaction) {
                        ssOwner.sendMessage("Money transaction failed for player: " + share.getKey());
                        return false;
                    }
                }
            }
            if(totalPercentage != 100) {
                double amount = fPrice;
                if(totalPercentage > 0)
                    amount = (fPrice / 100 * (100 - totalPercentage));
                return ssOwner.mutateMoney(amount);
            } else
                return true;
        }
    }

    private static Map<String, Integer> getShares(Sign sign, SignShopPlayer ssPlayer) {
        List<Integer> tempperc = signshopUtil.getSharePercentages(sign.getLine(3));
        HashMap<String, Integer> shares = new HashMap<String, Integer>();

        if(tempperc.size() == 2 && signshopUtil.lineIsEmpty(sign.getLine(1)) && signshopUtil.lineIsEmpty(sign.getLine(2))) {
            ssPlayer.sendMessage("No usernames have been given on the second and third line so ignoring Share sign.");
            return shares;
        }
        if(tempperc.size() == 2 && (signshopUtil.lineIsEmpty(sign.getLine(1)) || lineIsEmpty(sign.getLine(2)))) {
            shares.put((sign.getLine(1) == null ? sign.getLine(2) : sign.getLine(1)), tempperc.get(0));
            ssPlayer.sendMessage("The second percentage will be ignored as only one username is given.");
        } else if(tempperc.size() == 1 && !signshopUtil.lineIsEmpty(sign.getLine(2))) {
            shares.put(sign.getLine(1), tempperc.get(0));
            ssPlayer.sendMessage("The second username will be ignored as only one percentage is given.");
        } else if(tempperc.size() == 2) {
            shares.put(sign.getLine(1), tempperc.get(0));
            shares.put(sign.getLine(2), tempperc.get(1));
        } else if(tempperc.size() == 1) {
            shares.put(sign.getLine(1), tempperc.get(0));
        }
        return shares;
    }
}

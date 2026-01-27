package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.events.SSMoneyRequestType;
import org.wargamer2010.signshop.events.SSMoneyTransactionEvent;
import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wargamer2010.signshop.util.signshopUtil.getSignsFromMisc;
import static org.wargamer2010.signshop.util.signshopUtil.lineIsEmpty;

/**
 * Internal listener that distributes shop earnings among multiple players using ShareSign percentage splits.
 */
public class SharedMoneyTransaction implements Listener {
    private static boolean distributeMoney(Seller seller, double fPrice, SignShopPlayer ssPlayer) {
        List<Block> shareSigns = getSignsFromMisc(seller, "sharesigns");
        SignShopPlayer ssOwner = seller.getOwner();
        if(shareSigns.isEmpty()) {
            return ssOwner.mutateMoney(fPrice);
        } else {
            boolean bTotalTransaction;
            Map<String, Integer> shares = new HashMap<>();
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
        List<Integer> tempperc = signshopUtil.getSharePercentages(sign.getSide(Side.FRONT).getLine(3));
        HashMap<String, Integer> shares = new HashMap<>();

        if(tempperc.size() == 2 && signshopUtil.lineIsEmpty(sign.getSide(Side.FRONT).getLine(1)) && signshopUtil.lineIsEmpty(sign.getSide(Side.FRONT).getLine(2))) {
            ssPlayer.sendMessage("No usernames have been given on the second and third line so ignoring Share sign.");
            return shares;
        }
        if(tempperc.size() == 2 && (signshopUtil.lineIsEmpty(sign.getSide(Side.FRONT).getLine(1)) || lineIsEmpty(sign.getSide(Side.FRONT).getLine(2)))) {
            shares.put((sign.getSide(Side.FRONT).getLine(1) == null ? sign.getSide(Side.FRONT).getLine(2) : sign.getSide(Side.FRONT).getLine(1)), tempperc.getFirst());
            ssPlayer.sendMessage("The second percentage will be ignored as only one username is given.");
        } else if(tempperc.size() == 1 && !signshopUtil.lineIsEmpty(sign.getSide(Side.FRONT).getLine(2))) {
            shares.put(sign.getSide(Side.FRONT).getLine(1), tempperc.getFirst());
            ssPlayer.sendMessage("The second username will be ignored as only one percentage is given.");
        } else if(tempperc.size() == 2) {
            shares.put(sign.getSide(Side.FRONT).getLine(1), tempperc.get(0));
            shares.put(sign.getSide(Side.FRONT).getLine(2), tempperc.get(1));
        } else if(tempperc.size() == 1) {
            shares.put(sign.getSide(Side.FRONT).getLine(1), tempperc.getFirst());
        }
        return shares;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSSMoneyTransaction(SSMoneyTransactionEvent event) {
        if (event.isHandled() || event.isCancelled() || event.getPlayer().getPlayer() == null)
            return;
        if (event.getShop() == null || !event.getShop().hasMisc("sharesigns"))
            return;
        if (event.isNotBalanceOrExecution())
            return;

        if (event.getRequestType() == SSMoneyRequestType.CheckBalance) {
            switch (event.getTransactionType()) {
                case GiveToOwner:
                    // This is tricky, we'd have to check whether all the people we're sharing with could
                    // take their share of the money
                    break;
                case TakeFromOwner:
                case GiveToPlayer:
                case TakeFromPlayer:
                case Unknown:
                    return;
            }
        }
        else {
            boolean bTransaction = false;

            switch (event.getTransactionType()) {
                case GiveToOwner:
                    bTransaction = distributeMoney(event.getShop(), event.getPrice(), event.getPlayer());
                    break;
                case TakeFromOwner:
                case Unknown:
                case TakeFromPlayer:
                case GiveToPlayer:
                    return;
            }

            if (!bTransaction) {
                event.getPlayer().sendMessage("The money transaction failed, please contact the System Administrator");
                event.setCancelled(true);
            }
        }

        event.setHandled(true);
    }
}

package org.wargamer2010.signshop.listeners.sslisteners;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.events.SSMoneyRequestType;
import org.wargamer2010.signshop.events.SSMoneyTransactionEvent;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.itemUtil;

import java.util.LinkedList;
import java.util.List;

import static org.wargamer2010.signshop.util.signshopUtil.getSignsFromMisc;

/**
 * Handles Vault economy integration for SignShop transactions.
 *
 * <p>Responds to {@link SSMoneyTransactionEvent} to check balances and
 * execute money transfers through the Vault economy provider.</p>
 */
public class BankTransaction implements Listener {
    private static List<String> getBanks(Seller seller) {
        List<Block> bankSigns = getSignsFromMisc(seller, "banksigns");
        List<String> banks = new LinkedList<>();
        if (!bankSigns.isEmpty()) {
            for (Block banksign : bankSigns) {
                if (itemUtil.clickedSign(banksign)) {
                    Sign sign = (Sign) banksign.getState();
                    if (sign.getSide(Side.FRONT).getLine(1) != null)
                        banks.add(sign.getSide(Side.FRONT).getLine(1));
                }
            }
        }
        return banks;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSSMoneyTransaction(SSMoneyTransactionEvent event) {
        if(event.isHandled() || event.isCancelled() || event.getPlayer().getPlayer() == null)
            return;
        if(event.getShop() == null || !event.getShop().hasMisc("banksigns"))
            return;
        if (event.isNotBalanceOrExecution())
            return;

        if(!Vault.getEconomy().hasBankSupport()) {
            event.getPlayer().sendMessage("The current Economy plugin offers no Bank support!");
            return;
        }

        List<String> banks = getBanks(event.getShop());
        if(banks.isEmpty())
            return;

        List<String> ownedBanks = new LinkedList<>();
        SignShopPlayer ssOwner = event.getShop().getOwner();
        for(String bank : banks) {
            OfflinePlayer owner = event.getShop().getOwner().getOfflinePlayer();
            if(Vault.getEconomy().isBankOwner(bank, owner).transactionSuccess() || Vault.getEconomy().isBankMember(bank, owner).transactionSuccess()
                    || ssOwner.isOp(event.getPlayer().getWorld())) {
                ownedBanks.add(bank);
            } else {
                event.setMessagePart("!bank", bank);
                ssOwner.sendMessage(SignShop.getInstance().getSignShopConfig().getError("not_allowed_to_use_bank", event.getMessageParts()));
            }
        }

        if(ownedBanks.isEmpty()) {
            event.setHandled(true);
            event.setCancelled(true);
            event.getPlayer().sendMessage(SignShop.getInstance().getSignShopConfig().getError("no_bank_available", event.getMessageParts()));
            return;
        }


        if(event.getRequestType() == SSMoneyRequestType.CheckBalance) {
            switch(event.getTransactionType()) {
                case GiveToOwner:
                    // Does a Bank have a money cap?
                break;
                case TakeFromOwner:
                    boolean hasTheMoney = true;
                    for(String bank : banks) {
                        EconomyResponse response = Vault.getEconomy().bankHas(bank, event.getPrice());
                        if(response.transactionSuccess()) {
                            hasTheMoney = true;
                            break;
                        }
                    }
                    if(!hasTheMoney)
                        event.setCancelled(true);
                break;
                case GiveToPlayer:
                case Unknown:
                case TakeFromPlayer:
                    return;
            }
        } else {
            boolean bTransaction = false;

            switch(event.getTransactionType()) {
                case GiveToOwner:
                    for(String bank : banks) {
                        EconomyResponse response = Vault.getEconomy().bankDeposit(bank, event.getPrice());
                        if(response.transactionSuccess()) {
                            bTransaction = true;
                            break;
                        }
                    }
                break;
                case TakeFromOwner:
                    for(String bank : banks) {
                        EconomyResponse response = Vault.getEconomy().bankHas(bank, event.getPrice());
                        if(response.transactionSuccess()) {
                            EconomyResponse second = Vault.getEconomy().bankWithdraw(bank, event.getPrice());
                            if(second.transactionSuccess()) {
                                bTransaction = true;
                                break;
                            }
                        }
                    }
                break;
                case GiveToPlayer:
                case Unknown:
                case TakeFromPlayer:
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

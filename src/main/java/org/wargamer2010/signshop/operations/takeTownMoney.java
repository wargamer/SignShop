package org.wargamer2010.signshop.operations;


import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.milkbowl.vault.economy.EconomyResponse;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.events.SSMoneyEventType;
import org.wargamer2010.signshop.money.MoneyModifierManager;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.economyUtil;

/**
 * Shop operation that withdraws money from a Towny town bank account.
 */
@SuppressWarnings("deprecation") //As far as I know Towny only has name based town economy
public class takeTownMoney implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        ssArgs.setMoneyEventType(SSMoneyEventType.TakeFromTown);
        ssArgs.setMessagePart("!price", economyUtil.formatMoney(ssArgs.getPrice().get()));
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        SignShopPlayer ssPlayer = ssArgs.getPlayer().get();
        if (!ssArgs.isPlayerOnline())
            return true;

        double fPrice = MoneyModifierManager.applyModifiers(ssArgs, SSMoneyEventType.TakeFromTown);

        try {
            if (!TownySettings.getTownBankAllowWithdrawls()) {
                ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("towny_bank_withdrawls_not_allowed", ssArgs.getMessageParts()));
                return false;
            }
            Resident resident = TownyUniverse.getInstance().getResident(ssArgs.getOwner().get().getName());
            Town town = resident.getTown();
            if (!resident.isMayor()) {
                if (!town.hasTrustedResident(resident)) {
                    ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("towny_owner_not_mayor_or_assistant", ssArgs.getMessageParts()));
                    return false;
                }
            }
            if (!Vault.getEconomy().has(town.getAccount().getName(), fPrice)) {
                ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("no_shop_money", ssArgs.getMessageParts()));
                return false;
            }
        } catch (TownyException x) {
            // TownyMessaging.sendErrorMsg(player, x.getMessage());
            ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("towny_owner_not_belong_to_town", ssArgs.getMessageParts()));
            return false;
        }

        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        SignShopPlayer ssPlayer = ssArgs.getPlayer().get();
        if (ssPlayer == null)
            return true;

        double fPrice = MoneyModifierManager.applyModifiers(ssArgs, SSMoneyEventType.TakeFromTown);

        // first withdraw money from the bank
        Resident resident;
        Town town;
        EconomyResponse response;
        try {
            if (!TownySettings.getTownBankAllowWithdrawls()) {
                ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("towny_bank_withdrawls_not_allowed", ssArgs.getMessageParts()));
                return false;
            }

            resident = TownyUniverse.getInstance().getResident(ssArgs.getOwner().get().getName());
            town = resident.getTown();

            // take the money from the town bank account
            response = Vault.getEconomy().withdrawPlayer(town.getAccount().getName(), fPrice);
            if (response.type != EconomyResponse.ResponseType.SUCCESS) {
                ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("towny_insufficient_funds", ssArgs.getMessageParts()));
                return false;
            }

            return true;
        } catch (TownyException x) {
            // TownyMessaging.sendErrorMsg(ssPlayer.getPlayer(), x.getMessage());
            ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("towny_owner_not_belong_to_town", ssArgs.getMessageParts()));
            return false;
        }
    }
}

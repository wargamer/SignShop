package org.wargamer2010.signshop.operations;

import net.milkbowl.vault.economy.EconomyResponse;

import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.util.economyUtil;

import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.events.SSMoneyEventType;
import org.wargamer2010.signshop.money.MoneyModifierManager;

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
            if(!ssArgs.isPlayerOnline())
                return true;

            Double fPrice = MoneyModifierManager.applyModifiers(ssArgs, SSMoneyEventType.TakeFromTown);

            try {
                if (!TownySettings.getTownBankAllowWithdrawls()) {
                    ssPlayer.sendMessage(SignShopConfig.getError("towny_bank_withdrawls_not_allowed", ssArgs.getMessageParts()));
                    return false;
                }
                Resident resident = TownyUniverse.getDataSource().getResident(ssArgs.getOwner().get().getName());
                Town town = resident.getTown();
                if (!resident.isMayor()) {
                    if (!town.hasAssistant(resident)) {
                        ssPlayer.sendMessage(SignShopConfig.getError("towny_owner_not_mayor_or_assistant", ssArgs.getMessageParts()));
                        return false;
                    }
                }
                if (!Vault.getEconomy().has(town.getEconomyName(), fPrice)) {
                    ssPlayer.sendMessage(SignShopConfig.getError("no_shop_money", ssArgs.getMessageParts()));
                    return false;
                }
            } catch (TownyException x) {
                // TownyMessaging.sendErrorMsg(player, x.getMessage());
                ssPlayer.sendMessage(SignShopConfig.getError("towny_owner_not_belong_to_town", ssArgs.getMessageParts()));
                return false;
            }

            return true;
	}

	@Override
	public Boolean runOperation(SignShopArguments ssArgs) {
            SignShopPlayer ssPlayer = ssArgs.getPlayer().get();
            if (ssPlayer == null)
                    return true;

            Double fPrice = MoneyModifierManager.applyModifiers(ssArgs, SSMoneyEventType.TakeFromTown);

            // first withdraw money from the bank
            Resident resident;
            Town town;
            EconomyResponse response;
            try {
                if (!TownySettings.getTownBankAllowWithdrawls()) {
                    ssPlayer.sendMessage(SignShopConfig.getError("towny_bank_withdrawls_not_allowed", ssArgs.getMessageParts()));
                    return false;
                }

                resident = TownyUniverse.getDataSource().getResident(ssArgs.getOwner().get().getName());
                town = resident.getTown();

                // take the money from the town bank account
                response = Vault.getEconomy().withdrawPlayer(town.getEconomyName(), fPrice);
                if (response.type != EconomyResponse.ResponseType.SUCCESS) {
                    ssPlayer.sendMessage(SignShopConfig.getError("towny_insufficient_funds", ssArgs.getMessageParts()));
                    return false;
                }

                return true;
            } catch (TownyException x) {
                // TownyMessaging.sendErrorMsg(ssPlayer.getPlayer(), x.getMessage());
                ssPlayer.sendMessage(SignShopConfig.getError("towny_owner_not_belong_to_town", ssArgs.getMessageParts()));
                return false;
            }
	}
}

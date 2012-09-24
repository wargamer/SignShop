package org.wargamer2010.signshop.operations;

import net.milkbowl.vault.economy.EconomyResponse;

import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.util.economyUtil;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import org.wargamer2010.signshop.configuration.SignShopConfig;

public class takeTownMoney implements SignShopOperation {
	@Override
	public Boolean setupOperation(SignShopArguments ssArgs) {
            ssArgs.setMessagePart("!price", economyUtil.formatMoney(ssArgs.get_fPrice()));
            return true;
	}

	@Override
	public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
            SignShopPlayer ssPlayer = ssArgs.get_ssPlayer();
            if (ssPlayer.getPlayer() == null)
                return true;

            Float fPricemod = ssArgs.get_ssPlayer().getPlayerPricemod(ssArgs.get_sOperation(), false);
            Float fPrice = (ssArgs.get_fPrice_root() * fPricemod);
            ssArgs.set_fPrice(fPrice);
            ssArgs.setMessagePart("!price", economyUtil.formatMoney(fPrice));

            try {
                if (!TownySettings.getTownBankAllowWithdrawls()) {
                    ssPlayer.sendMessage(SignShopConfig.getError("towny_bank_withdrawls_not_allowed", ssArgs.messageParts));
                    return false;
                }
                Resident resident = TownyUniverse.getDataSource().getResident(ssPlayer.getName());
                Town town = resident.getTown();
                if (!resident.isMayor()) {
                    if (!town.hasAssistant(resident)) {
                        ssPlayer.sendMessage(SignShopConfig.getError("towny_owner_not_mayor_or_assistant", ssArgs.messageParts));
                        return false;
                    }
                }
                if (!Vault.economy.has(town.getEconomyName(), fPrice)) {
                    ssPlayer.sendMessage(SignShopConfig.getError("no_shop_money", ssArgs.messageParts));
                    return false;
                }
            } catch (TownyException x) {
                // TownyMessaging.sendErrorMsg(player, x.getMessage());
                ssPlayer.sendMessage(SignShopConfig.getError("towny_owner_not_belong_to_town", ssArgs.messageParts));
                return false;
            }

            return true;
	}

	@Override
	public Boolean runOperation(SignShopArguments ssArgs) {
            SignShopPlayer ssPlayer = ssArgs.get_ssPlayer();
            if (ssPlayer == null)
                    return true;

            Float fPricemod = ssArgs.get_ssPlayer().getPlayerPricemod(ssArgs.get_sOperation(), false);
            Float fPrice = (ssArgs.get_fPrice_root() * fPricemod);
            ssArgs.set_fPrice(fPrice);
            ssArgs.setMessagePart("!price", economyUtil.formatMoney(fPrice));

            // first withdraw money from the bank
            Resident resident;
            Town town;
            EconomyResponse response;
            try {
                if (!TownySettings.getTownBankAllowWithdrawls()) {
                    ssPlayer.sendMessage(SignShopConfig.getError("towny_bank_withdrawls_not_allowed", ssArgs.messageParts));
                    return false;
                }

                resident = TownyUniverse.getDataSource().getResident(ssArgs.get_ssOwner().getName());
                town = resident.getTown();

                // take the money from the town bank account
                response = Vault.economy.withdrawPlayer(town.getEconomyName(), fPrice);
                if (response.type != EconomyResponse.ResponseType.SUCCESS) {
                    ssPlayer.sendMessage(SignShopConfig.getError("towny_insufficient_funds", ssArgs.messageParts));
                    return false;
                }

                return true;
            } catch (TownyException x) {
                // TownyMessaging.sendErrorMsg(ssPlayer.getPlayer(), x.getMessage());
                ssPlayer.sendMessage(SignShopConfig.getError("towny_owner_not_belong_to_town", ssArgs.messageParts));
                return false;
            }
	}
}

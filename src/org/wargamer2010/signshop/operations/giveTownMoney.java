package org.wargamer2010.signshop.operations;

import net.milkbowl.vault.economy.EconomyResponse;

import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.player.SignShopPlayer;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import org.wargamer2010.signshop.events.SSMoneyEventType;
import org.wargamer2010.signshop.money.MoneyModifierManager;

public class giveTownMoney implements SignShopOperation {
	@Override
	public Boolean setupOperation(SignShopArguments ssArgs) {
            ssArgs.setMoneyEventType(SSMoneyEventType.GiveToTown);
            ssArgs.setMessagePart("!price", economyUtil.formatMoney(ssArgs.getPrice().get()));
            return true;
	}

	@Override
	public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
            SignShopPlayer ssPlayer = ssArgs.getPlayer().get();
            if(!ssArgs.isPlayerOnline())
                return true;

            MoneyModifierManager.applyModifiers(ssArgs, SSMoneyEventType.GiveToTown);

            try {
                Resident resident = TownyUniverse.getDataSource().getResident(ssArgs.getOwner().get().getName());
                Town town = resident.getTown();
                if (!resident.isMayor()) {
                    if (!town.hasAssistant(resident)) {
                        ssPlayer.sendMessage(SignShopConfig.getError("towny_owner_not_mayor_or_assistant", ssArgs.getMessageParts()));
                        return false;
                    }
                }
                if (Vault.getEconomy() == null) {
                    ssPlayer.sendMessage("Error with the economy, tell the System Administrator to install Vault properly.");
                    return false;
                } else if (town.getEconomyName().isEmpty()) {
                    ssPlayer.sendMessage(SignShopConfig.getError("towny_owner_not_belong_to_town", ssArgs.getMessageParts()));
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
            if (ssPlayer == null) {
                    return false;
            }

            Double fPrice = MoneyModifierManager.applyModifiers(ssArgs, SSMoneyEventType.GiveToTown);

            // then deposit it into the bank
            Resident resident;
            Town town;
            try {
                resident = TownyUniverse.getDataSource().getResident(ssArgs.getOwner().get().getName());
                town = resident.getTown();

                double bankcap = TownySettings.getTownBankCap();
                if (bankcap > 0) {
                    if (fPrice + town.getHoldingBalance() > bankcap)
                        throw new TownyException(String.format(TownySettings.getLangString("msg_err_deposit_capped"), bankcap));
                }

                EconomyResponse response = Vault.getEconomy().depositPlayer(town.getEconomyName(), fPrice);
                if (response.type != EconomyResponse.ResponseType.SUCCESS) {
                    ssPlayer.sendMessage("Error depositing into shop owners account!");
                    return false;
                }

                return true;
            } catch (TownyException x) {
                TownyMessaging.sendErrorMsg(ssPlayer.getPlayer(), x.getMessage());
            } catch (EconomyException x) {
                TownyMessaging.sendErrorMsg(ssPlayer.getPlayer(), x.getMessage());
            }
            return false;
	}
}

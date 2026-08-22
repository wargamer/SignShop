package org.wargamer2010.signshop.operations;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import net.milkbowl.vault.economy.EconomyResponse;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.events.SSMoneyEventType;
import org.wargamer2010.signshop.money.MoneyModifierManager;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.economyUtil;

/**
 * Shop operation that deposits money into a Towny town bank account.
 */
@SuppressWarnings("deprecation") //As far as I know Towny only has name based town economy
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
                Resident resident = TownyUniverse.getInstance().getResident(ssArgs.getOwner().get().getName());
                Town town = resident.getTown();
                if (!resident.isMayor()) {
                    if (!town.hasTrustedResident(resident)) {
                        ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("towny_owner_not_mayor_or_assistant", ssArgs.getMessageParts()));
                        return false;
                    }
                }
                if (Vault.getEconomy() == null) {
                    ssPlayer.sendMessage("Error with the economy, tell the System Administrator to install Vault properly.");
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
            if (ssPlayer == null) {
                    return false;
            }

        double fPrice = MoneyModifierManager.applyModifiers(ssArgs, SSMoneyEventType.GiveToTown);

            // then deposit it into the bank
            Resident resident;
            Town town;
            try {
                resident = TownyUniverse.getInstance().getResident(ssArgs.getOwner().get().getName());
                town = resident.getTown();

                double bankcap = TownySettings.getTownBankCap();
                if (bankcap > 0) {
                    if (fPrice + town.getAccount().getHoldingBalance() > bankcap)
                        throw new TownyException(String.format(String.valueOf(Translatable.of("msg_err_deposit_capped")), bankcap));
                }


                EconomyResponse response = Vault.getEconomy().depositPlayer(town.getAccount().getName(), fPrice);
                if (response.type != EconomyResponse.ResponseType.SUCCESS) {
                    ssPlayer.sendMessage("Error depositing into shop owners account!");
                    return false;
                }

                return true;
            } catch (TownyException x) {
                TownyMessaging.sendErrorMsg(ssPlayer.getPlayer(), x.getMessage());
            }
        return false;
	}
}


package org.wargamer2010.signshop.money;

import org.wargamer2010.signshop.events.SSMoneyEventType;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.signshopUtil;

/**
 * Price modifier that applies player-specific or permission-based price adjustments.
 *
 * <p>Applies discounts or surcharges based on player permissions and operation type.
 * Uses the PriceMultipliers configuration section in config.yml.</p>
 *
 * @see MoneyModifierManager
 * @see IMoneyModifier
 * @see signshopUtil#ApplyPriceMod
 */
public class PlayerPriceModifier implements IMoneyModifier {

    @Override
    public double applyModifier(SignShopPlayer player, double fPrice, String sOperation, SSMoneyEventType type) {
        return signshopUtil.ApplyPriceMod(player, fPrice, sOperation, isBuyOperation(type));
    }

    @Override
    public void applyModifier(SignShopArguments ssArgs, SSMoneyEventType type) {
        signshopUtil.ApplyPriceMod(ssArgs, isBuyOperation(type));
    }

    private boolean isBuyOperation(SSMoneyEventType type) {
        return (type == SSMoneyEventType.TakeFromPlayer
                || type == SSMoneyEventType.TakeFromTown
                || type == SSMoneyEventType.GiveToOwner);
    }
}

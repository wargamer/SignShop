
package org.wargamer2010.signshop.money;

import org.wargamer2010.signshop.events.SSMoneyEventType;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.signshopUtil;

public class PlayerPriceModifier implements IMoneyModifier {

    @Override
    public double applyModifier(SignShopPlayer player, double fPrice, String sOperation, SSMoneyEventType type) {
        return signshopUtil.ApplyPriceMod(player, fPrice, sOperation, isBuyOperation(type));
    }

    @Override
    public double applyModifier(SignShopArguments ssArgs, SSMoneyEventType type) {
        return signshopUtil.ApplyPriceMod(ssArgs, isBuyOperation(type));
    }

    private boolean isBuyOperation(SSMoneyEventType type) {
        return (type == SSMoneyEventType.TakeFromPlayer
                || type == SSMoneyEventType.TakeFromTown
                || type == SSMoneyEventType.GiveToOwner);
    }
}

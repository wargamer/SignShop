
package org.wargamer2010.signshop.money;

import org.wargamer2010.signshop.events.SSMoneyEventType;
import static org.wargamer2010.signshop.events.SSMoneyEventType.GiveToOwner;
import static org.wargamer2010.signshop.events.SSMoneyEventType.GiveToTown;
import static org.wargamer2010.signshop.events.SSMoneyEventType.TakeFromOwner;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class RoundPriceModifier implements IMoneyModifier {

    @Override
    public double applyModifier(SignShopPlayer player, double fPrice, String sOperation, SSMoneyEventType type) {
        // When a player has to pay it should always be rounded up
        // When a player receives money it should always be rounded down
        // This prevents exploits where the player can profit from improper rounding
        switch(type) {
            case GiveToOwner:
            case TakeFromTown:
            case TakeFromPlayer:
                return roundToTwoDigits(fPrice, false);
            case TakeFromOwner:
            case GiveToTown:
            case GiveToPlayer:
                return roundToTwoDigits(fPrice, true);
            default:
                return fPrice;
        }
    }

    @Override
    public double applyModifier(SignShopArguments ssArgs, SSMoneyEventType type) {
        double newPrice = applyModifier(ssArgs.getPlayer().get(), ssArgs.getPrice().get(), ssArgs.getOperation().get(), type);
        ssArgs.getPrice().set(newPrice);
        return newPrice;
    }

    private static double roundToTwoDigits(double value, boolean roundDown) {
        if(value < 0.005)
            return 0.0d;
        if((((value) % 0.01) - 0.01) > -.0001)
            return value;
        double modifier = roundDown ? (-0.5) : (0.5);
        return Math.floor(value * 100 + modifier) / 100;
    }
}

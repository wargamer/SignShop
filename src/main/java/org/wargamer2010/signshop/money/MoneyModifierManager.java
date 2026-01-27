
package org.wargamer2010.signshop.money;

import org.wargamer2010.signshop.events.SSMoneyEventType;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.player.SignShopPlayer;

import java.util.LinkedList;
import java.util.List;

/**
 * Manages price modifiers applied to SignShop transactions.
 *
 * <p>Registers and applies modifiers like player-specific discounts and price
 * rounding. Modifiers are applied in order during transactions.</p>
 *
 * @see IMoneyModifier
 * @see PlayerPriceModifier
 * @see RoundPriceModifier
 */
public class MoneyModifierManager {
    private static final List<IMoneyModifier> modifiers = new LinkedList<>();

    private MoneyModifierManager() {

    }

    public static void init() {
        modifiers.add(new PlayerPriceModifier());
        modifiers.add(new RoundPriceModifier());
    }

    public static double applyModifiers(SignShopPlayer player, double fPrice, String sOperation, SSMoneyEventType type) {
        double result = fPrice;
        for(IMoneyModifier modifier : modifiers) {
            result = modifier.applyModifier(player, result, sOperation, type);
        }
        return result;
    }

    public static double applyModifiers(SignShopArguments ssArgs, SSMoneyEventType type) {
        for(IMoneyModifier modifier : modifiers) {
            modifier.applyModifier(ssArgs, type);
        }
        return ssArgs.getPrice().get();
    }
}

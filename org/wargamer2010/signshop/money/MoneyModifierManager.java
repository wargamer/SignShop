
package org.wargamer2010.signshop.money;

import java.util.LinkedList;
import java.util.List;
import org.wargamer2010.signshop.events.SSMoneyEventType;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class MoneyModifierManager {
    private static final List<IMoneyModifier> modifiers = new LinkedList<IMoneyModifier>();

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

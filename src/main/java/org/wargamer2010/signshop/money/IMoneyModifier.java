
package org.wargamer2010.signshop.money;

import org.wargamer2010.signshop.events.SSMoneyEventType;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.player.SignShopPlayer;

/**
 * Interface for transaction price modifiers.
 *
 * <p>Implementations can adjust prices based on player permissions, shop type,
 * or other factors. Applied via {@link MoneyModifierManager}.</p>
 */
public interface IMoneyModifier {
    double applyModifier(SignShopPlayer player, double fPrice, String sOperation, SSMoneyEventType type);

    void applyModifier(SignShopArguments ssArgs, SSMoneyEventType type);
}

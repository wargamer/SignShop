
package org.wargamer2010.signshop.money;

import org.wargamer2010.signshop.events.SSMoneyEventType;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.player.SignShopPlayer;

public interface IMoneyModifier {
    public double applyModifier(SignShopPlayer player, double fPrice, String sOperation, SSMoneyEventType type);

    public double applyModifier(SignShopArguments ssArgs, SSMoneyEventType type);
}

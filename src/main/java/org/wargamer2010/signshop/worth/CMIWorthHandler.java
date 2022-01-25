package org.wargamer2010.signshop.worth;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Worth.WorthItem;
import com.Zrips.CMI.Modules.Worth.WorthManager;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;

import java.util.logging.Level;

public class CMIWorthHandler implements WorthHandler {
    private final WorthManager worthManager;

    public CMIWorthHandler() {
        worthManager = CMI.getInstance().getWorthManager();
    }


    @Override
    public double getPrice(ItemStack stack) {
        WorthItem worthItem = worthManager.getWorth(stack);
        double d = worthItem == null ? 0 : worthItem.getSellPrice();

        if (SignShopConfig.debugging()) {
            SignShop.log(stack.getType() + " is worth " + d, Level.INFO);
        }
        return d;
    }
}

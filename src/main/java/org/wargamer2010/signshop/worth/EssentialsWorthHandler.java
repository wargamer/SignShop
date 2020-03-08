package org.wargamer2010.signshop.worth;


import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Worth;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

public class EssentialsWorthHandler implements WorthHandler {
    IEssentials ess;
    Worth worth;

    public EssentialsWorthHandler() {
        ess = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
        worth = ess.getWorth();
    }

    @Override
    public double getPrice(ItemStack stack) {

        BigDecimal worthPrice = worth.getPrice(ess, stack);
        return (worthPrice == null ? 0 : worthPrice.doubleValue());
    }
}

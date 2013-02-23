package org.wargamer2010.signshop.util;

import org.bukkit.ChatColor;
import org.wargamer2010.signshop.Vault;

public class economyUtil {
    private economyUtil() {

    }

    public static String formatMoney(Float money) {
        if(money.isNaN() || money.isInfinite())
            return "0.00";
        if(Vault.economy == null)
            return Float.toString(money);
        else
            return Vault.economy.format(money.doubleValue());
    }

    public static float parsePrice(String line) {
        String priceline = ChatColor.stripColor(line);
        String sPrice = "";
        Float fPrice;
        for(int i = 0; i < priceline.length(); i++)
            if(Character.isDigit(priceline.charAt(i)) || priceline.charAt(i) == '.')
                sPrice += priceline.charAt(i);
        try {
            fPrice = Float.parseFloat(sPrice);
        }
        catch(NumberFormatException nFE) {
            fPrice = 0.0f;
        }
        if(fPrice < 0.0f) {
            fPrice = 0.0f;
        }
        if(fPrice.isNaN() || fPrice.isInfinite())
            fPrice = 0.0f;
        return fPrice.floatValue();
    }

}

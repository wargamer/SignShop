package org.wargamer2010.signshop.util;

import org.bukkit.ChatColor;
import org.wargamer2010.signshop.Vault;

public class economyUtil {
    private static ChatColor moneyColor = ChatColor.GREEN;

    private economyUtil() {

    }

    private static String attachColor(String money) {
        return (moneyColor + money + ChatColor.WHITE);
    }

    public static String formatMoney(Float money) {
        if(money.isNaN() || money.isInfinite())
            return attachColor("0.00");
        if(Vault.getEconomy() == null)
            return attachColor(Float.toString(money));
        else
            return attachColor(Vault.getEconomy().format(money.doubleValue()));
    }

    public static float parsePrice(String line) {
        if(line == null)
            return 0.0f;
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

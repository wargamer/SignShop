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

    public static String formatMoney(double money) {
        if(Vault.getEconomy() == null)
            return attachColor(Double.toString(money));
        else
            return attachColor(Vault.getEconomy().format(money));
    }

    public static double parsePrice(String line) {
        if(line == null)
            return 0.0d;
        String priceline = ChatColor.stripColor(line);
        String sPrice = "";
        Double fPrice;
        for(int i = 0; i < priceline.length(); i++)
            if(Character.isDigit(priceline.charAt(i)) || priceline.charAt(i) == '.')
                sPrice += priceline.charAt(i);
        try {
            fPrice = Double.parseDouble(sPrice);
        }
        catch(NumberFormatException nFE) {
            fPrice = 0.0d;
        }
        if(fPrice < 0.0f) {
            fPrice = 0.0d;
        }
        if(fPrice.isNaN() || fPrice.isInfinite())
            fPrice = 0.0d;
        return fPrice.doubleValue();
    }

}

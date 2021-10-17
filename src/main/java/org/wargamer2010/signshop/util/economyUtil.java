package org.wargamer2010.signshop.util;

import org.bukkit.ChatColor;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.configuration.SignShopConfig;

public class economyUtil {

    private economyUtil() {

    }

    private static String attachColor(String money) {
        return (SignShopConfig.getMoneyColor() + money + ChatColor.WHITE);
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
        StringBuilder sPrice = new StringBuilder();
        Double fPrice;
        for(int i = 0; i < priceline.length(); i++)
            if(Character.isDigit(priceline.charAt(i)) || priceline.charAt(i) == '.')
                sPrice.append(priceline.charAt(i));
        try {
            fPrice = Double.parseDouble(sPrice.toString());
        }
        catch(NumberFormatException nFE) {
            fPrice = 0.0d;
        }
        if(fPrice < 0.0f) {
            fPrice = 0.0d;
        }
        if(Double.isNaN(fPrice) || fPrice.isInfinite())
            fPrice = 0.0d;
        return fPrice;
    }

}

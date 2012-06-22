package org.wargamer2010.signshop.util;

import org.bukkit.ChatColor;
import org.wargamer2010.signshop.Vault;

public class economyUtil {
    public static String formatMoney(double money) {
        if(Vault.economy == null)
            return Double.toString(money);
        else
            return Vault.economy.format(money);
    }
    
    public static float parsePrice(String priceline) {
        priceline = ChatColor.stripColor(priceline);
        String sPrice = "";
        float fPrice = 0.0f;
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
        return fPrice;
    }
    
}

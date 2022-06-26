package org.wargamer2010.signshop.util;

import org.bukkit.ChatColor;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.configuration.SignShopConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            if(Character.isDigit(priceline.charAt(i)) || priceline.charAt(i) == '.' || (SignShopConfig.allowCommaDecimalSeperator() && priceline.charAt(i) == ','))
                sPrice.append(priceline.charAt(i));
        if (SignShopConfig.allowCommaDecimalSeperator()) return parsePriceInternational(sPrice.toString());
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

    private static double parsePriceInternational(String price) {
        SignShop.debugMessage("Parsing a price from '" + price + "'");

        if (price == null || price.equals("")) {
            SignShop.debugMessage("Empty! (no price found)");
            return 0.0d;
        }

        // Count the number, and last known position of each type of delimiter
        Matcher periodFinder = Pattern.compile("[.]").matcher(price);
        Matcher commaFinder = Pattern.compile("[,]").matcher(price);

        int totalPeriods = 0;
        int lastPeriod = 0;
        int totalCommas = 0;
        int lastComma = 0;

        while (periodFinder.find()) {
            lastPeriod = periodFinder.start();
            totalPeriods++;
        }
        while (commaFinder.find()) {
            lastComma = commaFinder.start();
            totalCommas++;
        }

        // Now, the fun begins.

        double parsedPrice;

        try {

            if (totalPeriods == 0 && totalCommas == 0) {
                // If there are no delimiters, just parse the price
                SignShop.debugMessage(price + " is not delimited");
                parsedPrice = Double.parseDouble(price);
            } else {
                // There are delimiters, determine what kind

                /*
                    If the comma comes last, and there is only one comma, it is *probably* comma seperated
                        Sidenote: it doesn't matter if not actually comma seperated, the parser will fix that later
                    If there are no commas and more than one period, it has to be a comma seperated number
                        A comma seperated number cannot be valid with more than one comma,
                        the same way that a period seperated number cannot be valid with more than one period.

                    This method does not select a guess that is theoretically impossible (contains more than one decimal seperator),
                    although it will default to attempting to parse a period seperated number
                */
                boolean likelyCommaSeperated = (lastComma > lastPeriod && totalCommas == 1) || (totalCommas == 0 && totalPeriods > 1);

                SignShop.debugMessage("Likely: " + (likelyCommaSeperated ? "COMMA SEPERATED" : "PERIOD SEPERATED"));

                /*
                    Start of String
                    Group 1- All digits and divisions up to a decimal seperator
                    NCG- A single decimal seperator
                      Group 2- 2 digits after the decimals
                    End of String

                    These regexs must match the ENTIRE string. If a complete match is not found it is considered invalid.
                    This ensures that there is only one decimal seperator. Having 2+ is not valid

                    Period-seperated Parser uses ',' as a division, and '.' as a decimal seperator
                    Comma-seperated Parser uses '.' as a division, and ',' as a decimal seperator
                 */
                Matcher periodSeperatedParser = Pattern.compile("^([\\d,]*+)(?:[.](\\d{0,2}))?$").matcher(price);
                Matcher commaSeperatedParser = Pattern.compile("^([\\d.]*+)(?:[,](\\d{0,2}))?$").matcher(price);

                String periodSeperatedString = periodSeperatedParser.matches() ? price.replace(",", "") : null;
                String commaSeperatedString = commaSeperatedParser.matches() ? price.replace(".", "").replace(",", ".") : null;

                String priceString = null;

                if (likelyCommaSeperated && commaSeperatedString != null) {
                    // If the price was guessed to be comma-seperated and the comma-seperated regex passed, its comma-seperated
                    SignShop.debugMessage("Actual: COMMA SEPERATED");
                    priceString = commaSeperatedString;
                } else if (periodSeperatedString != null) {
                    // If the price was guessed to be period-seperated or the comma-seperated regex failed
                    // AND the period-seperated regex passed, its period-seperated
                    SignShop.debugMessage("Actual: PERIOD SEPERATED");
                    priceString = periodSeperatedString;
                } else {
                    // If the price has not yet been parsed, attempt error correction
                    if (!likelyCommaSeperated && commaSeperatedString != null) {
                        // Detection made a mistake, price was guessed as period-seperated, but the period-seperated regex failed and the comma-seperated passed. Its comma-seperated
                        SignShop.debugMessage("Actual: COMMA SEPERATED");
                        priceString = commaSeperatedString;
                    } else {
                        // No valid match could be parsed
                        SignShop.debugMessage("Actual: INVALID");
                    }
                }

                // Attempt to parse the price
                parsedPrice = priceString != null ? Double.parseDouble(priceString) : 0.0D;
            }
        } catch(NumberFormatException nFE) {
            // something was wrong... don't even ask what cause I don't know
            parsedPrice = 0.0d;
        }

        // Normalize
        if(parsedPrice < 0.0d || Double.isNaN(parsedPrice) || Double.isInfinite(parsedPrice)) parsedPrice = 0.0d;

        SignShop.debugMessage("Parsed price: " + parsedPrice);

        return parsedPrice;
    }

}

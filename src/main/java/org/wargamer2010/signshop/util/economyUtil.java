package org.wargamer2010.signshop.util;

import org.bukkit.ChatColor;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.configuration.SignShopConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for economy operations and price formatting.
 *
 * <p>Provides money formatting, price caching, and currency display helpers
 * that integrate with Vault's economy system.</p>
 *
 * @see Vault
 */
public class economyUtil {
    public static final Map<String, Double> priceCache = new HashMap<>();
    private static SignShopConfig signShopConfig;

    private economyUtil() {

    }

    public static void setSignShopConfig(SignShopConfig config) {
        signShopConfig = config;
    }

    private static String attachColor(String money) {
        return (signShopConfig.getMoneyColor() + money + ChatColor.WHITE);
    }

    /**
     * Formats a money amount using Vault's economy formatter.
     * Applies configured money color for display in chat messages.
     *
     * @param money The amount to format
     * @return Colored, formatted money string (e.g., "§a$100.00§f")
     */
    public static String formatMoney(double money) {
        if (Vault.getEconomy() == null)
            return attachColor(Double.toString(money));
        else
            return attachColor(Vault.getEconomy().format(money));
    }

    /**
     * Parses a price from a sign line, supporting both period and comma decimal separators.
     * Extracts numeric characters and handles international number formats.
     * Results are cached for performance when cachePrices is enabled.
     *
     * @param line The sign line containing the price (e.g., "$100" or "100,50")
     * @return Parsed price as double, or 0.0 if invalid
     */
    public static double parsePrice(String line) {
        if (line == null)
            return 0.0d;
        if (signShopConfig.cachePrices() && priceCache.containsKey(line)) return priceCache.get(line);
        String priceline = ChatColor.stripColor(line);
        StringBuilder sPrice = new StringBuilder();
        Double fPrice;
        for(int i = 0; i < priceline.length(); i++)
            if (Character.isDigit(priceline.charAt(i)) || priceline.charAt(i) == '.' || (signShopConfig.allowCommaDecimalSeparator().isPermitted() && priceline.charAt(i) == ','))
                sPrice.append(priceline.charAt(i));
        if (signShopConfig.allowCommaDecimalSeparator().isPermitted()) {
            double price = parsePriceInternational(sPrice.toString());
            if (signShopConfig.cachePrices()) priceCache.put(line, price);
            return price;
        }
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

        if (signShopConfig.cachePrices()) priceCache.put(line, fPrice);
        return fPrice;
    }

    private static double parsePriceInternational(String price) {

        if (price == null || price.isEmpty()) {
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
                parsedPrice = Double.parseDouble(price);
            } else {
                // There are delimiters, determine what kind

                /*
                    If the comma comes last, and there is only one comma, it is *probably* comma separated
                        Sidenote: it doesn't matter if not actually comma separated, the parser will fix that later
                    If there are no commas and more than one period, it has to be a comma separated number
                        A comma separated number cannot be valid with more than one comma,
                        the same way that a period separated number cannot be valid with more than one period.

                    This method does not select a guess that is theoretically impossible (contains more than one decimal separator),
                    although it will default to attempting to parse a period separated number
                */
                boolean likelyCommaSeparated = (lastComma > lastPeriod && totalCommas == 1) || (totalCommas == 0 && totalPeriods > 1);

                /*
                    Start of String
                    Group 1- All digits and divisions up to a decimal separator
                    NCG- A single decimal separator
                      Group 2- 2 digits after the decimals
                    End of String

                    These regexs must match the ENTIRE string. If a complete match is not found it is considered invalid.
                    This ensures that there is only one decimal separator. Having 2+ is not valid

                    Period-separated Parser uses ',' as a division, and '.' as a decimal separator
                    Comma-separated Parser uses '.' as a division, and ',' as a decimal separator
                 */
                Matcher periodSeparatedParser = Pattern.compile("^([\\d,]*+)(?:[.](\\d{0,2}))?$").matcher(price);
                Matcher commaSeparatedParser = Pattern.compile("^([\\d.]*+)(?:[,](\\d{0,2}))?$").matcher(price);

                String periodSeparatedString = periodSeparatedParser.matches() ? price.replace(",", "") : null;
                String commaSeparatedString = commaSeparatedParser.matches() ? price.replace(".", "").replace(",", ".") : null;

                String priceString = null;

                if (likelyCommaSeparated && commaSeparatedString != null) {
                    // If the price was guessed to be comma-separated and the comma-separated regex passed, its comma-separated
                    priceString = commaSeparatedString;
                } else if (periodSeparatedString != null) {
                    // If the price was guessed to be period-separated or the comma-separated regex failed
                    // AND the period-separated regex passed, its period-separated
                    priceString = periodSeparatedString;
                } else {
                    // If the price has not yet been parsed, attempt error correction
                    if (!likelyCommaSeparated && commaSeparatedString != null) {
                        // Detection made a mistake, price was guessed as period-separated, but the period-separated regex failed and the comma-separated passed. Its comma-separated
                        priceString = commaSeparatedString;
                    } else {
                        // No valid match could be parsed
                        SignShop.getInstance().debugMessage("Parsed price was INVALID");
                    }
                }

                // Attempt to parse the price
                parsedPrice = priceString != null ? Double.parseDouble(priceString) : 0.0D;
            }
        } catch(NumberFormatException nFE) {
            // something was wrong... don't even ask what because I don't know
            parsedPrice = 0.0d;
        }
        // Normalize
        if(parsedPrice < 0.0d || Double.isNaN(parsedPrice) || Double.isInfinite(parsedPrice)) parsedPrice = 0.0d;

        return parsedPrice;
    }

}

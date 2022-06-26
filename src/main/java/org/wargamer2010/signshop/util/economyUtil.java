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
        if (SignShopConfig.useInternationalCurrencyParser()) return parsePriceInternational(line);
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

    public static double parsePriceInternational(String line) {
        if (SignShopConfig.debugging()) SignShop.debugMessage("Parsing a price from '" + line + "'");
        /*
            Start of String
            NCG- All non-numeric characters up to the first digit
            Capture Group- All sequential numerals, commas, periods, and spaces
            NCG- Any characters after the price (having a group of numerals seperated from the price will cause the method to fail)
        */
        Matcher priceFinder = Pattern.compile("^[\\D]*+([\\d\\s,.]*+)[\\D]*+$").matcher(ChatColor.stripColor(line));

        if (!priceFinder.matches()) {
            if (SignShopConfig.debugging()) SignShop.debugMessage(line + " does not contain a valid price");
            return 0.0d;
        }

        // Get the captured price from the regex
        String price = priceFinder.group(1);

        if (price == null || price.equals("")) {
            if (SignShopConfig.debugging()) SignShop.debugMessage(line + " does not contain a price");
            return 0.0d;
        }
        // Strip out spaces
        else price = price.replace(" ", "");

        if (SignShopConfig.debugging()) SignShop.debugMessage("Detected price: " + price);

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
                if (SignShopConfig.debugging()) SignShop.debugMessage(price + " is not delimited");
                parsedPrice = Double.parseDouble(price);
            } else {
                // There are delimiters, determine what kind

            /*
                If the comma comes last, and there is only one comma, it is *probably* international
                    Sidenote: it doesn't matter if not actually comma delimited, the parser will fix that later
                If there are no commas and more than one period, it has to be an international number
                    An internation number cannot be valid with more than one comma,
                    the same way that a US number cannot be valid with more than one period.

                This method does not select a guess that is theoretically impossible (contains more than one decimal seperator),
                although it will default to attempting to parse a US delimited number
            */
                boolean likelyInternational = (lastComma > lastPeriod && totalCommas == 1) || (totalCommas == 0 && totalPeriods > 1);

                if (SignShopConfig.debugging())
                    SignShop.debugMessage("Likely: " + (likelyInternational ? "INTERNATIONAL" : "NOT INTERNATIONAL"));

            /*
                Start of String
                Group 1- All digits and divisions up to a decimal seperator
                NCG- A single decimal seperator
                  Group 2- 2 digits after the decimals
                End of String

                These regexs must match the ENTIRE string. If a complete match is not found it is considered invalid.
                This ensures that there is only one decimal seperator. Having 2+ is not valid

                US Parser uses ',' as a division, and '.' as a decimal seperator
                EU Parser uses '.' as a division, and ',' as a decimal seperator
             */
                Matcher usParser = Pattern.compile("^([\\d,]*+)(?:[.](\\d{0,2}))?$").matcher(price);
                Matcher euParser = Pattern.compile("^([\\d.]*+)(?:[,](\\d{0,2}))?$").matcher(price);

                String usPriceString = usParser.matches() ? price.replace(",", "") : null;
                String euPriceString = euParser.matches() ? price.replace(".", "").replace(",", ".") : null;

                String priceString = null;

                if (likelyInternational && euPriceString != null) {
                    // If the price was guessed to be international and the EU regex passed, its international
                    if (SignShopConfig.debugging()) SignShop.debugMessage("Actual: INTERNATIONAL");
                    priceString = euPriceString;
                } else if (usPriceString != null) {
                    // If the price was guessed to be not international or the EU regex failed
                    // AND the US regex passed, its US
                    if (SignShopConfig.debugging()) SignShop.debugMessage("Actual: NOT INTERNATIONAL");
                    priceString = usPriceString;
                }

                if (priceString == null) {
                    // If the price has not yet been parsed, attempt error correction
                    if (!likelyInternational && euPriceString != null) {
                        // Detection made a mistake, price was guessed as non-international, but the US regex failed and the EU passed. Its international
                        if (SignShopConfig.debugging()) SignShop.debugMessage("Actual: INTERNATIONAL");
                        priceString = euPriceString;
                    } else {
                        // No valid match could be parsed
                        if (SignShopConfig.debugging()) SignShop.debugMessage("Actual: INVALID");
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
        if(parsedPrice < 0.0d) parsedPrice = 0.0d;
        if(Double.isNaN(parsedPrice) || Double.isInfinite(parsedPrice)) parsedPrice = 0.0d;

        return parsedPrice;
    }

}

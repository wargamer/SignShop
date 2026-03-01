package org.wargamer2010.signshop.money;

/**
 * Result of parsing a price from a sign line, including the matched currency.
 *
 * @param price    The parsed numeric price
 * @param currency The currency matched from the symbol/abbreviation on the sign line
 */
public record PriceResult(double price, CurrencyDefinition currency) {
}
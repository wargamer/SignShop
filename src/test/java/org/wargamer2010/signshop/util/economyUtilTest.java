package org.wargamer2010.signshop.util;

import junit.framework.TestCase;
import org.wargamer2010.signshop.configuration.SignShopConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class economyUtilTest extends TestCase {
    /**
     * Test the international price parser.
     */
    public void testparsePrice() {
        SignShopConfig.CommaDecimalSeparatorState prev = SignShopConfig.allowCommaDecimalSeparator();
        SignShopConfig.setAllowCommaDecimalSeparator(SignShopConfig.CommaDecimalSeparatorState.TRUE, false);
        assertEquals(0.0D, economyUtil.parsePrice(null));
        assertEquals(0.0D, economyUtil.parsePrice("null"));
        assertEquals(0.0D, economyUtil.parsePrice("NaN"));
        assertEquals(5.0D, economyUtil.parsePrice("-5"));
        assertEquals(1234.0D, economyUtil.parsePrice("1234"));
        assertEquals(1234.0D, economyUtil.parsePrice("1234.00"));
        assertEquals(1234.0D, economyUtil.parsePrice("1234,00"));
        assertEquals(1234.0D, economyUtil.parsePrice("1,234.00"));
        assertEquals(1234.0D, economyUtil.parsePrice("1.234,00"));
        assertEquals(1234.0D, economyUtil.parsePrice("1 234.00"));
        assertEquals(1234.0D, economyUtil.parsePrice("1 234,00"));
        assertEquals(1234.0D, economyUtil.parsePrice("1, 234.00"));
        assertEquals(1234.0D, economyUtil.parsePrice("1. 234,00"));
        assertEquals(123400.0D, economyUtil.parsePrice("1, 234,00"));
        assertEquals(123400.0D, economyUtil.parsePrice("1. 234.00"));
        assertEquals(123400.0D, economyUtil.parsePrice("1. 234. 00"));
        assertEquals(123400.0D, economyUtil.parsePrice("1, 234, 00"));
        assertEquals(1234.0D, economyUtil.parsePrice("wa 1234"));
        assertEquals(1234.0D, economyUtil.parsePrice("wa 1234 wa"));
        assertEquals(1234.0D, economyUtil.parsePrice("wa 1234.00"));
        assertEquals(1234.0D, economyUtil.parsePrice("wa 1234,00"));
        assertEquals(1234.0D, economyUtil.parsePrice("wa 1234.00 wa"));
        assertEquals(1234.0D, economyUtil.parsePrice("wa 1234,00 wa"));
        assertEquals(1234.0D, economyUtil.parsePrice("wa 1,234 wa"));
        assertEquals(1234.0D, economyUtil.parsePrice("wa 1.234 wa"));
        assertEquals(1234.0D, economyUtil.parsePrice("1,,,,,234"));
        assertEquals(1234.0D, economyUtil.parsePrice("1.....234"));
        assertEquals(1234.0D, economyUtil.parsePrice("1,2,3,4,,,"));
        assertEquals(1234.0D, economyUtil.parsePrice("1.2.3.4..."));
        assertEquals(1234.0D, economyUtil.parsePrice(",,,1,2,3,4"));
        assertEquals(1234.0D, economyUtil.parsePrice("...1.2.3.4"));
        assertEquals(0.0D, economyUtil.parsePrice("..,.,.,.1234,.,.,.,"));
        assertEquals(0.0D, economyUtil.parsePrice("1,.2,.3,.4,."));
        assertEquals(0.0D, economyUtil.parsePrice("1.,2.,3.,4.,"));
        assertEquals(12341234.0D, economyUtil.parsePrice("wa 1234 wa 1234"));
        assertEquals(12341234.0D, economyUtil.parsePrice("1234 wa 1234 wa"));
        assertEquals(121212.0D, economyUtil.parsePrice("12 wa 12 wa 12 wa"));
        assertEquals(121212.0D, economyUtil.parsePrice("wa 12 wa 12 wa 12"));
        assertEquals(12341234.0D, economyUtil.parsePrice("wa, 12,3,4 w,a 1,2,34,"));
        assertEquals(12341234.0D, economyUtil.parsePrice("1234. wa. 1.2.3.4 .wa."));
        assertEquals(1212.12D, economyUtil.parsePrice("12. wa 12. wa, 12 wa"));
        assertEquals(1212.12D, economyUtil.parsePrice("wa, 12, wa 1,2 wa. 12"));
        assertEquals(0.0D, economyUtil.parsePrice("12. wa 1,2 wa 12 wa"));
        assertEquals(0.0D, economyUtil.parsePrice("wa, 12, wa 1.2 wa 12"));
        assertEquals(1234.0D, economyUtil.parsePrice("!@#$%^&*()1234!@#$%^&*()"));
        assertEquals(0.0D, economyUtil.parsePrice(""));
        assertEquals(0.0D, economyUtil.parsePrice("i am nothing"));
        assertEquals(40711031.0D, economyUtil.parsePrice("i 4m n07h1ng w1th s0m3th1ng"));
        assertEquals(43.0D, economyUtil.parsePrice("giggity goo ga 43"));
        assertEquals(12341234.0D, economyUtil.parsePrice("1234.1234"));
        assertEquals(12341234.0D, economyUtil.parsePrice("1234,1234"));
        assertEquals(12341234.0D, economyUtil.parsePrice("1234+1234"));
        SignShopConfig.setAllowCommaDecimalSeparator(prev, false);
    }

    public void testPriceCaching() {
        int testSize = 3000000, runs = 5;
        runParserCacheTests(false, testSize, runs);
        runParserCacheTests(true, testSize, runs);
    }

    public void runParserCacheTests(boolean oldParser, int testSize, int runs) {
        System.out.println("Parser  : " + (oldParser ? "UNITED STATES" : "INTERNATIONAL"));
        System.out.println("Data set: " + testSize + " price(s)");
        System.out.println("Runs    : " + runs);

        // Set up the correct parser
        SignShopConfig.CommaDecimalSeparatorState state = SignShopConfig.allowCommaDecimalSeparator();
        if (oldParser) SignShopConfig.setAllowCommaDecimalSeparator(SignShopConfig.CommaDecimalSeparatorState.FALSE, false);
        else SignShopConfig.setAllowCommaDecimalSeparator(SignShopConfig.CommaDecimalSeparatorState.TRUE, false);

        // Data storage for run statistics
        List<PriceCacheTestData> runData = new ArrayList<>();

        /*
        Generate Input Data
         */

        String[] testData = new String[testSize];
        for (int i = 0; i < testData.length; i++) testData[i] = randomPriceString();

        /*
        Run the parser and collect data
         */
        for (int run = 0; run < runs; run++) {
            System.out.println("Executing: Run " + (run + 1) + " / " + runs);

            // Clear the cache
            economyUtil.priceCache.clear();

            // Data variables
            long timePriceParseBegin, timePriceParseEnd,
                    timePriceParseCacheBegin, timePriceParseCacheEnd,
                    timeCacheHitBegin, timeCacheHitEnd;
            long cacheSize;

            /*
            Parse Data, no cache
             */

            // Time before parsing
            timePriceParseBegin = System.currentTimeMillis();

            double[] parsed = parsePrices(testData, false);

            // Time after parsing
            timePriceParseEnd = System.currentTimeMillis();

            /*
            Parse data, with cache
             */

            // Measure memory before data is parsed with cache
            long preParseCacheMemory = usedMemory();

            // Time before parsing with cache
            timePriceParseCacheBegin = System.currentTimeMillis();

            parsePrices(testData, true);

            // Time after parsing
            timePriceParseCacheEnd = System.currentTimeMillis();

            // Measure memory after data is parsed with cache
            cacheSize = usedMemory() - preParseCacheMemory;

            /*
            Hit the cached data
             */

            // Time before hitting cache
            timeCacheHitBegin = System.currentTimeMillis();

            parsePrices(testData, true);

            // Time after cache hit
            timeCacheHitEnd = System.currentTimeMillis();

            /*
            Test is complete
             */
            runData.add(new PriceCacheTestData(timePriceParseBegin, timePriceParseEnd,
                    timePriceParseCacheBegin, timePriceParseCacheEnd,
                    timeCacheHitBegin, timeCacheHitEnd,
                    cacheSize));
        }

        SignShopConfig.setAllowCommaDecimalSeparator(state, false);

        System.out.println("Aggregating data...");
        Statistics parseTimes = statistics(runData.stream().mapToLong((data) -> data.timePriceParseEnd - data.timePriceParseBegin).toArray());
        Statistics cachedParseTimes = statistics(runData.stream().mapToLong((data) -> data.timePriceParseCacheEnd - data.timePriceParseCacheBegin).toArray());
        Statistics cacheHitTimes = statistics(runData.stream().mapToLong((data) -> data.timeCacheHitEnd - data.timeCacheHitBegin).toArray());
        Statistics cacheSizes = statistics(runData.stream().mapToLong(PriceCacheTestData::getCacheSize).toArray());

        System.out.println("Test Complete. Results:");
        System.out.println();
        System.out.println("Average time to parse prices (no cache)  : " + parseTimes.mean + "ms");
        System.out.println("  Deviation                              : " + parseTimes.standardDeviation + "ms");
        System.out.println();
        System.out.println("Average time to parse prices (with cache): " + cachedParseTimes.mean + "ms");
        System.out.println("  Deviation                              : " + cachedParseTimes.standardDeviation + "ms");
        System.out.println();
        System.out.println("Average time to hit cache                : " + cacheHitTimes.mean + "ms");
        System.out.println("  Deviation                              : " + cacheHitTimes.standardDeviation + "ms");
        System.out.println();
        System.out.println("Average total cache size                 : " + cacheSizes.mean + "MB");
        System.out.println("  Deviation                              : " + cacheSizes.standardDeviation + "MB");
    }

    /**
     * Parse an array of prices
     * @param prices The data to be parsed
     * @param useCache If the cache should be used
     * @return An array of parsed prices
     */
    private double[] parsePrices(String[] prices, boolean useCache) {
        boolean prev = SignShopConfig.CachePrices();
        SignShopConfig.setCachePrices(useCache);

        double[] parsed = new double[prices.length];
        for (int i = 0; i < prices.length; i++) {
            parsed[i] = economyUtil.parsePrice(prices[i]);
        }

        SignShopConfig.setCachePrices(prev);

        return parsed;
    }

    /**
     * Random alphanumeric price string
     * @return A 20-character alpha-numeric string
     */
    private String randomPriceString() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 20;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    /**
     * Get amount of memory currently in use
     * @return Used memory (in MB)
     */
    private long usedMemory() {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1000000;
    }

    /*
    Data storage
     */
    static class PriceCacheTestData {
        // Data variables
        long timePriceParseBegin;
        long timePriceParseEnd;
        long timePriceParseCacheBegin;
        long timePriceParseCacheEnd;
        long timeCacheHitBegin;
        long timeCacheHitEnd;
        long cacheSize;

        public long getTimePriceParseBegin() {
            return timePriceParseBegin;
        }

        public long getTimePriceParseEnd() {
            return timePriceParseEnd;
        }

        public long getTimePriceParseCacheBegin() {
            return timePriceParseCacheBegin;
        }

        public long getTimePriceParseCacheEnd() {
            return timePriceParseCacheEnd;
        }

        public long getTimeCacheHitBegin() {
            return timeCacheHitBegin;
        }

        public long getTimeCacheHitEnd() {
            return timeCacheHitEnd;
        }

        public long getCacheSize() {
            return cacheSize;
        }

        PriceCacheTestData(long timePriceParseBegin, long timePriceParseEnd,
                           long timePriceParseCacheBegin, long timePriceParseCacheEnd,
                           long timeCacheHitBegin, long timeCacheHitEnd,
                           long cacheSize) {
            this.timePriceParseBegin = timePriceParseBegin;
            this.timePriceParseEnd = timePriceParseEnd;
            this.timePriceParseCacheBegin = timePriceParseCacheBegin;
            this.timePriceParseCacheEnd = timePriceParseCacheEnd;
            this.timeCacheHitBegin = timeCacheHitBegin;
            this.timeCacheHitEnd = timeCacheHitEnd;
            this.cacheSize = cacheSize;
        }
    }

    /*
    Statistics calculator
     */

    static class Statistics {
        long mean, standardDeviation;

        Statistics(long mean, long standardDeviation) {
            this.mean = mean;
            this.standardDeviation = standardDeviation;
        }
    }

    private static Statistics statistics(long[] numArray) {
        double sum = 0.0, standardDeviation = 0.0;
        int length = numArray.length;

        for (long num : numArray) {
            sum += num;
        }

        double mean = sum / length;

        for (long num : numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return new Statistics((long) mean, (long) Math.sqrt(standardDeviation / length));
    }
}
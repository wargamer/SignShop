package org.wargamer2010.signshop.util;

import com.opencsv.CSVWriter;
import junit.framework.TestCase;
import org.wargamer2010.signshop.configuration.SignShopConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class EconomyUtilTests extends TestCase {
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

    /**
     * Test the efficiency of the parsers, with and without caching enabled
     */
    public void testPriceCaching() {
        runBatchParserCacheTests(
                new boolean[]{true, false},
                new int[]{1000, 10000},
                new int[]{10, 55, 100},
                new Order[]{Order.GROUPED, Order.SEQUENTIAL, Order.RANDOM}
        );
    }

    /**
     * Test the parsers. This function will iterate over every possible permutation of the provided input parameters, calling {@link EconomyUtilTests#runParserCacheTest(boolean, int, List)}
     * The function will generate one random set of data for each permutation of the test size, number of variations, and order of the variations
     *  @param newParserValues Possible values for "use new parser"
     * @param testSizeValues  A set of values for the size of tests to execute
     * @param variationValues A set of values for the number of different prices to be tested
     * @param orderValues     A set of values for the order of the prices to be tested
     */
    private void runBatchParserCacheTests(boolean[] newParserValues, int[] testSizeValues, int[] variationValues, Order[] orderValues) {
        // Create a list to store all the test data
        List<ParserCacheTestData> allTestData = new ArrayList<>();

        // For every test size
        for (int testSize : testSizeValues) {
            // Test every number of variations
            for (int variation : variationValues) {
                // Test in every order
                for (Order order : orderValues) {
                    /*
                    Generate Input Data (only 1 set of input data per permutation of test size, number of variations, and order
                     */
                    // Store the test values
                    List<String> testValues = new ArrayList<>();

                    if (variation > 0) {
                        // If we have a limited number of variations

                        // Generate the different variations
                        List<String> variations = new ArrayList<>();
                        for (int i = 0; i < variation; i++) variations.add(randomPriceString());

                        // Calculate how many times each variation will be used (only needs to be done once)
                        int repeats = (int) ((double) testSize / variation);
                        // Populate the test data with the variations
                        for (int i = 0; i < testSize; i++) {
                            // Calculate the variant to be used
                            int variant = (order == Order.GROUPED) ? i / repeats : i;

                            // Normalize (loop repeatedly from the first variant to the last)
                            variant %= variation;

                            // Add the variant
                            testValues.add(variations.get(variant));
                        }

                        // If it's random, shuffle it.
                        if (order == Order.RANDOM) Collections.shuffle(testValues);
                    } else {
                        // Infinite variations
                        for (int i = 0; i < testSize; i++) testValues.add(randomPriceString());
                    }

                    // Test every parser variant
                    for (boolean newParser : newParserValues) {
                        // Run the test
                        ParserCacheTestData testData = runParserCacheTest(newParser, 10, testValues);

                        // Set some metadata about the test
                        testData.testSize = testSize;
                        testData.variation = variation;
                        testData.order = order;

                        // Calculate the statistics
                        testData.calculate();

                        // Store the test
                        allTestData.add(testData);
                    }

                }
            }
        }

        // Save the data to a CSV
        try {
            // Get a file and a CSV writer
            File f = new File("ParserCacheTest" + System.currentTimeMillis() + ".csv");
            CSVWriter writer = new CSVWriter(new FileWriter(f));

            // Write the headers
            writer.writeNext(ParserCacheTestData.csvHeaders);

            // Write all the data sorted by parser version, then test size, then number of variations, then order of variations
            writer.writeAll(allTestData.stream()
                    .sorted(Comparator.comparing(ParserCacheTestData::getParser)
                            .thenComparing(ParserCacheTestData::getTestSize)
                            .thenComparing(ParserCacheTestData::getVariation)
                            .thenComparing(ParserCacheTestData::getOrder))
                    .map(ParserCacheTestData::csvData).collect(Collectors.toList()));

            // Close the writer
            writer.close();

            System.out.println("Saved data to " + f.getCanonicalPath());
        } catch (IOException e) {
            System.out.println("Failed to write CSV file");
        }
    }

    private ParserCacheTestData runParserCacheTest(boolean newParser, int runs, List<String> testValues) {
        // Set up the correct parser
        SignShopConfig.CommaDecimalSeparatorState state = SignShopConfig.allowCommaDecimalSeparator();
        if (newParser)
            SignShopConfig.setAllowCommaDecimalSeparator(SignShopConfig.CommaDecimalSeparatorState.TRUE, false);
        else SignShopConfig.setAllowCommaDecimalSeparator(SignShopConfig.CommaDecimalSeparatorState.FALSE, false);

        // Data storage for run statistics
        ParserCacheTestData testData = new ParserCacheTestData();

        // Set some metadata for the test
        testData.newParser = newParser;
        testData.runs = runs;

        /*
        Run the parser and collect data
         */
        for (int run = 0; run < runs; run++) {
            // Clear the cache
            economyUtil.priceCache.clear();

            // Data variables
            ParserCacheTestRunData runData = new ParserCacheTestRunData();

            /*
            Parse Data, no cache
             */

            // Time before parsing
            runData.timePriceParseBegin = System.currentTimeMillis();

            parsePrices(testValues, false);

            // Time after parsing
            runData.timePriceParseEnd = System.currentTimeMillis();

            /*
            Parse data, with cache
             */

            // Measure memory before data is parsed with cache
            long preParseCacheMemory = usedMemory();

            // Time before parsing with cache
            runData.timePriceParseCacheBegin = System.currentTimeMillis();

            List<Double> ignored = parsePrices(testValues, true);

            // Time after parsing
            runData.timePriceParseCacheEnd = System.currentTimeMillis();

            // Measure memory after data is parsed with cache
            runData.cacheSize = usedMemory() - preParseCacheMemory;

            /*
            Hit the cached data
             */

            // Time before hitting cache
            runData.timeCacheHitBegin = System.currentTimeMillis();

            parsePrices(testValues, true);

            // Time after cache hit
            runData.timeCacheHitEnd = System.currentTimeMillis();

            /*
            Test is complete
             */
            testData.runData.add(runData);
        }

        // Reset the parser, just in case
        SignShopConfig.setAllowCommaDecimalSeparator(state, false);

        return testData;
    }

    /**
     * Parse an array of prices
     *
     * @param prices   The data to be parsed
     * @param useCache If the cache should be used
     */
    private List<Double> parsePrices(List<String> prices, boolean useCache) {
        boolean prev = SignShopConfig.cachePrices();
        SignShopConfig.setCachePrices(useCache);

        List<Double> parsed = prices.stream().map(economyUtil::parsePrice).collect(Collectors.toList());

        SignShopConfig.setCachePrices(prev);

        return parsed;
    }

    /**
     * Random alphanumeric price string
     * <br>7 letter prefix
     * <br>A number between 0.0 and 10000.0, with a 50% chance of being period seperated and a 50% chance of being comma seperated
     * <br>7 letter suffix
     * @return A 20-character alphanumeric string
     */
    private String randomPriceString() {
        int targetStringLength = 7;
        Random random = new Random();

        // Generate 7 random integers corresponding to Unicode a-zA-Z
        String prefix = random.ints('A', 'z')
                .filter(i -> (i <= 'Z' || i >= 'a'))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        // Random double between 0.0 and 10000.0
        String number = String.valueOf(random.nextInt(10000) + random.nextDouble());

        // 50% chance to be comma seperated, 50% chance to be period seperated
        if (random.nextDouble() > 0.5) number = number.replace('.', ',');

        // Generate 7 random integers corresponding to Unicode a-zA-Z
        String suffix = random.ints('A', 'z')
                .filter(i -> (i <= 'Z' || i >= 'a'))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return prefix + number + suffix;
    }

    /**
     * Get amount of memory currently in use
     *
     * @return Used memory (in KB)
     */
    private long usedMemory() {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1000;
    }

    private enum Order {
        GROUPED,
        SEQUENTIAL,
        RANDOM
    }

    /*
    Data storage
     */
    private static class ParserCacheTestData {
        static String[] csvHeaders = {"New Parser", "# of Runs", "Test Size", "Variations", "Order",
                "Avg. Parse Time", "Parse Time Deviation",
                "Avg. Parse Time w/ Cache", "Parse Time w/ Cache Deviation",
                "Avg. Cache Hit Time", "Cache Hit Time Deviation",
                "Avg. Cache Size", "Cache Size Deviation"
        };

        boolean newParser;

        boolean getParser() {
            return newParser;
        }

        int testSize, runs, variation;

        int getTestSize() {
            return testSize;
        }

        int getRuns() {
            return runs;
        }

        int getVariation() {
            return variation;
        }

        Order order;

        Order getOrder() {
            return order;
        }

        List<ParserCacheTestRunData> runData = new ArrayList<>();

        NumeralArrayStatistic parseTimes, cachedParseTimes, cacheHitTimes, cacheSizes;

        void calculate() {
            parseTimes = new NumeralArrayStatistic(runData.stream().mapToLong(ParserCacheTestRunData::priceParseDuration).toArray());
            cachedParseTimes = new NumeralArrayStatistic(runData.stream().mapToLong(ParserCacheTestRunData::priceParseCacheDuration).toArray());
            cacheHitTimes = new NumeralArrayStatistic(runData.stream().mapToLong(ParserCacheTestRunData::cacheHitDuration).toArray());
            cacheSizes = new NumeralArrayStatistic(runData.stream().mapToLong(ParserCacheTestRunData::getCacheSize).toArray());
        }

        String[] csvData() {
            return new String[]{
                    String.valueOf(newParser),
                    String.valueOf(runs),
                    String.valueOf(testSize),
                    String.valueOf(variation),
                    order.toString(),
                    parseTimes.mean + "ms",
                    parseTimes.standardDeviation + "ms",
                    cachedParseTimes.mean + "ms",
                    cachedParseTimes.standardDeviation + "ms",
                    cacheHitTimes.mean + "ms",
                    cacheHitTimes.standardDeviation + "ms",
                    cacheSizes.mean + " KB",
                    cacheSizes.standardDeviation + " KB"
            };
        }
    }

    private static class ParserCacheTestRunData {
        // Data variables
        long timePriceParseBegin;
        long timePriceParseEnd;
        long timePriceParseCacheBegin;
        long timePriceParseCacheEnd;
        long timeCacheHitBegin;
        long timeCacheHitEnd;
        long cacheSize;

        public long priceParseDuration() {
            return timePriceParseEnd - timePriceParseBegin;
        }

        public long priceParseCacheDuration() {
            return timePriceParseCacheEnd - timePriceParseCacheBegin;
        }

        public long cacheHitDuration() {
            return timeCacheHitEnd - timeCacheHitBegin;
        }

        public long getCacheSize() {
            return cacheSize;
        }
    }

    /*
    Statistics calculator
     */

    private static class NumeralArrayStatistic {
        long[] data;
        long mean, standardDeviation;

        NumeralArrayStatistic(long[] arr) {
            data = arr;

            double sum = 0.0, standardDeviation = 0.0;

            for (long num : arr) sum += num;

            double mean = sum / arr.length;

            for (long num : arr) standardDeviation += Math.pow(num - mean, 2);

            this.mean = (long) mean;
            this.standardDeviation = (long) Math.sqrt(standardDeviation / arr.length);
        }
    }
}
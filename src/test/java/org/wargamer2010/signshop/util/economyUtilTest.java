package org.wargamer2010.signshop.util;

import com.opencsv.CSVWriter;
import junit.framework.TestCase;
import org.wargamer2010.signshop.configuration.SignShopConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
        runBatchParserCacheTests(
                new boolean[]{true, false},
                new int[]{1000, 10000},
                new int[]{10},
                new int[]{10, 55, 100},
                new Order[]{Order.GROUPED, Order.SEQUENTIAL, Order.RANDOM}
        );
    }

    private void runBatchParserCacheTests(boolean[] newParserValues, int[] testSizeValues, int[] runsValues, int[] variationValues, Order[] orderValues) {
        List<ParserCacheTestData> allTestData = new ArrayList<>();

        for (int testSize : testSizeValues) {
            for (int variation : variationValues) {
                for (Order order : orderValues) {
                    /*
                    Generate Input Data
                     */
                    List<String> testValues = new ArrayList<>();
                    if (variation > 0) {
                        List<String> variations = new ArrayList<>();
                        for (int i = 0; i < variation; i++) variations.add(randomPriceString());

                        int repeats = (int) ((double) testSize / variation);
                        int variant;

                        for (int i = 0; i < testSize; i++) {
                            if (order == Order.GROUPED) variant = i / repeats;
                            else variant = i;

                            // Normalize
                            variant %= variation;

                            testValues.add(variations.get(variant));
                        }

                        if (order == Order.RANDOM) Collections.shuffle(testValues);
                    } else {
                        for (int i = 0; i < testSize; i++) testValues.add(randomPriceString());
                    }

                    for (boolean newParser : newParserValues) {
                        for (int runs : runsValues) {
                            ParserCacheTestData testData = runParserCacheTest(newParser, runs, testValues);

                            testData.testSize = testSize;
                            testData.variation = variation;
                            testData.order = order;

                            testData.calculate();

                            allTestData.add(testData);
                        }
                    }

                }
            }
        }

        try {
            File f = new File("ParserCacheTest" + System.currentTimeMillis() + ".csv");
            CSVWriter writer = new CSVWriter(new FileWriter(f));

            writer.writeNext(ParserCacheTestData.csvHeaders);
            writer.writeAll(allTestData.stream()
                    .sorted(Comparator.comparing(ParserCacheTestData::getParser)
                            .thenComparing(ParserCacheTestData::getTestSize)
                            .thenComparing(ParserCacheTestData::getVariation)
                            .thenComparing(ParserCacheTestData::getOrder))
                    .map(ParserCacheTestData::csvData).collect(Collectors.toList()));
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
        boolean prev = SignShopConfig.CachePrices();
        SignShopConfig.setCachePrices(useCache);

        List<Double> parsed = prices.stream().map(economyUtil::parsePrice).collect(Collectors.toList());

        SignShopConfig.setCachePrices(prev);

        return parsed;
    }

    /**
     * Random alphanumeric price string
     *
     * @return A 20-character alpha-numeric string
     */
    private String randomPriceString() {
        int targetStringLength = 7;
        Random random = new Random();

        String prefix = random.ints('A', 'z')
                .filter(i -> (i <= 'Z' || i >= 'a'))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        String number = String.valueOf(random.nextInt(10000) + random.nextDouble());
        if (random.nextDouble() > 0.5) number = number.replace('.', ',');

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
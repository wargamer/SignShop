
package org.wargamer2010.signshop.configuration;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.signshopUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;

/**
 * Utility for leather armor color name lookups.
 *
 * <p>Loads color names from colors.yml based on the configured language,
 * with automatic fallback to en_US. Supports fuzzy matching to find the
 * closest color name for custom RGB values.</p>
 */
public class ColorUtil {
    private static final String DEFAULT_LANGUAGE = "en_US";
    private static final Map<Integer, String> colorLookup = new HashMap<>();

    private ColorUtil() {

    }

    /**
     * Initialize color lookup from colors.yml.
     * Uses two-layer loading: bundled JAR colors first, then user overrides from plugin folder.
     * Should be called after SignShopConfig is initialized.
     */
    public static void init() {
        colorLookup.clear();

        // Get the configured language
        String language = getPrimaryLanguage();

        // 1. Load bundled colors from JAR (read-only, does not write to disk)
        FileConfiguration jarConfig = loadConfigFromJar("colors.yml");
        if (jarConfig != null) {
            // Load stock colors for the configured language (decimal RGB keys)
            boolean loadedLanguage = loadStockColors(jarConfig, language);

            // If configured language wasn't found, load en_US
            if (!loadedLanguage && !language.equals(DEFAULT_LANGUAGE)) {
                loadStockColors(jarConfig, DEFAULT_LANGUAGE);
            }

            // Load extended colors for fuzzy matching (hex RGB keys) - don't overwrite stock
            loadExtendedColors(jarConfig, false);
        }

        // 2. Load user overrides from plugin folder (takes precedence)
        File userFile = new File(SignShop.getInstance().getDataFolder(), "colors.yml");
        if (userFile.exists()) {
            FileConfiguration userConfig = YamlConfiguration.loadConfiguration(userFile);

            // Load user stock colors (overrides bundled)
            boolean loadedUserLanguage = loadStockColors(userConfig, language);
            if (!loadedUserLanguage && !language.equals(DEFAULT_LANGUAGE)) {
                loadStockColors(userConfig, DEFAULT_LANGUAGE);
            }

            // Load user extended colors (overrides bundled) - allow overwrites
            loadExtendedColors(userConfig, true);
        }

        // 3. If no colors were loaded at all, use hardcoded defaults
        if (colorLookup.isEmpty()) {
            loadHardcodedDefaults();
        }
    }

    /**
     * Load stock Minecraft colors from the stock-colors section.
     *
     * @param config The configuration file
     * @param language The language to load
     * @return true if colors were loaded for this language
     */
    private static boolean loadStockColors(FileConfiguration config, String language) {
        ConfigurationSection section = config.getConfigurationSection("stock-colors." + language);
        if (section == null) {
            return false;
        }

        for (String key : section.getKeys(false)) {
            try {
                // Keys longer than 6 chars are decimal (legacy format); 6 chars or fewer are hex
                int rgb = (key.length() > 6) ? Integer.parseInt(key) : Integer.parseInt(key, 16);
                String colorName = section.getString(key, "");
                colorLookup.put(rgb, colorName);
            } catch (NumberFormatException ignored) {
                // Skip invalid keys
            }
        }
        return !section.getKeys(false).isEmpty();
    }

    /**
     * Load extended colors for fuzzy matching (hex RGB keys).
     * When loading from JAR, doesn't overwrite stock colors.
     * When loading from user file, allows overwrites.
     *
     * @param config The configuration to load from
     * @param allowOverwrite If true, overwrites existing entries (for user overrides)
     */
    private static void loadExtendedColors(FileConfiguration config, boolean allowOverwrite) {
        ConfigurationSection section = config.getConfigurationSection("extended-colors");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            try {
                int rgb = Integer.parseInt(key, 16);
                // Only add if not exists, unless allowOverwrite is true
                if (allowOverwrite || !colorLookup.containsKey(rgb)) {
                    String colorName = section.getString(key, "");
                    colorLookup.put(rgb, colorName);
                }
            } catch (NumberFormatException ignored) {
                // Skip invalid hex keys
            }
        }
    }

    /**
     * Gets the primary language from SignShop config.
     */
    private static String getPrimaryLanguage() {
        try {
            SignShopConfig config = SignShop.getInstance().getSignShopConfig();
            if (config != null) {
                String languages = config.getLanguages();
                if (languages != null && !languages.isEmpty()) {
                    String primary = languages.split(",")[0].trim();
                    if (primary.equalsIgnoreCase("english")) {
                        return DEFAULT_LANGUAGE;
                    }
                    return primary;
                }
            }
        } catch (Exception e) {
            // Config not yet initialized, use default
        }
        return DEFAULT_LANGUAGE;
    }

    /**
     * Load hardcoded defaults as fallback if colors.yml is missing or empty.
     * Uses MC 1.21 leather armor dye colors.
     */
    private static void loadHardcodedDefaults() {
        // Stock Minecraft 1.21 leather armor colors (16 dye colors)
        colorLookup.put(16383998, "white");       // #F9FFFE
        colorLookup.put(10329495, "light gray");  // #9D9D97
        colorLookup.put(4673362, "gray");         // #474F52
        colorLookup.put(1908001, "black");        // #1D1D21
        colorLookup.put(8606770, "brown");        // #835432
        colorLookup.put(11546150, "red");         // #B02E26
        colorLookup.put(16351261, "orange");      // #F9801D
        colorLookup.put(16701501, "yellow");      // #FED83D
        colorLookup.put(8439583, "lime");         // #80C71F
        colorLookup.put(6192150, "green");        // #5E7C16
        colorLookup.put(1481884, "cyan");         // #169C9C
        colorLookup.put(3847130, "light blue");   // #3AB3DA
        colorLookup.put(3949738, "blue");         // #3C44AA
        colorLookup.put(8991416, "purple");       // #8932B8
        colorLookup.put(13061821, "magenta");     // #C74EBD
        colorLookup.put(15961002, "pink");        // #F38BAA
    }

    /**
     * Load a configuration file from the JAR without writing to disk.
     * Unlike configUtil.loadYMLFromJar(), this method only reads and does not modify files.
     *
     * @param filename The filename to load from JAR resources
     * @return The loaded configuration, or null if not found
     */
    private static FileConfiguration loadConfigFromJar(String filename) {
        try {
            InputStream in = ColorUtil.class.getResourceAsStream("/" + filename);
            if (in != null) {
                FileConfiguration config = new YamlConfiguration();
                config.load(new InputStreamReader(in, StandardCharsets.UTF_8));
                in.close();
                return config;
            }
        } catch (IOException | InvalidConfigurationException e) {
            SignShop.log("Could not load " + filename + " from JAR: " + e.getMessage(), Level.WARNING);
        }
        return null;
    }

    /**
     * Get a human-readable color name for the given Bukkit Color.
     * Uses fuzzy matching to find the closest color if no exact match exists.
     *
     * @param color The Bukkit color to look up
     * @return The color name, capitalized
     */
    public static String getColorAsString(Color color) {
        int rgb = color.asRGB();
        if (colorLookup.containsKey(rgb)) {
            return signshopUtil.capFirstLetter(colorLookup.get(rgb));
        } else {
            // Fuzzy matching - find closest color
            double diff = -1;
            String last = "";
            for (int val : colorLookup.keySet()) {
                double currentdiff = getDifferenceBetweenColors(rgb, val);
                if (diff == -1 || currentdiff < diff) {
                    diff = currentdiff;
                    last = colorLookup.get(val);
                }
            }
            return signshopUtil.capFirstLetter(last);
        }
    }

    /**
     * Calculate the difference between two RGB colors.
     * Used for fuzzy color matching.
     */
    public static double getDifferenceBetweenColors(int colorone, int colortwo) {
        java.awt.Color a = new java.awt.Color(colorone);
        java.awt.Color b = new java.awt.Color(colortwo);
        int dr = a.getRed() - b.getRed();
        int dg = a.getGreen() - b.getGreen();
        int db = a.getBlue() - b.getBlue();
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }
}

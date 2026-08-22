package org.wargamer2010.signshop.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.wargamer2010.signshop.SignShop;

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
 * Utility for loading and retrieving localizable item meta format strings.
 *
 * <p>Loads format strings from meta.yml based on the configured language,
 * with automatic fallback to en_US if a format is not defined for the
 * current language.</p>
 */
public class MetaFormats {
    private static final String DEFAULT_LANGUAGE = "en_US";
    private static final Map<String, String> formats = new HashMap<>();
    private static final Map<String, String> defaultFormats = new HashMap<>();

    private MetaFormats() {
    }

    /**
     * Initialize format strings from meta.yml.
     * Uses two-layer loading: bundled JAR formats first, then user overrides from plugin folder.
     * Should be called after SignShopConfig is initialized.
     */
    public static void init() {
        formats.clear();
        defaultFormats.clear();

        // Get the configured language
        String language = getPrimaryLanguage();

        // 1. Load bundled formats from JAR (read-only, does not write to disk)
        FileConfiguration jarConfig = loadConfigFromJar("meta.yml");
        if (jarConfig != null) {
            loadFormatsFromConfig(jarConfig, language);
        }

        // 2. Load user overrides from plugin folder (takes precedence)
        File userFile = new File(SignShop.getInstance().getDataFolder(), "meta.yml");
        if (userFile.exists()) {
            FileConfiguration userConfig = YamlConfiguration.loadConfiguration(userFile);
            loadFormatsFromConfig(userConfig, language);
        }

        // 3. If no formats were loaded at all, use hardcoded defaults
        if (defaultFormats.isEmpty()) {
            loadHardcodedDefaults();
        }
    }

    /**
     * Load formats from a configuration file.
     */
    private static void loadFormatsFromConfig(FileConfiguration config, String language) {
        // Load default (en_US) formats
        ConfigurationSection defaultSection = config.getConfigurationSection("formats." + DEFAULT_LANGUAGE);
        if (defaultSection != null) {
            for (String key : defaultSection.getKeys(false)) {
                String value = defaultSection.getString(key, "");
                defaultFormats.put(key, value);
            }
        }

        // If the configured language is not en_US, load its formats
        if (!language.equals(DEFAULT_LANGUAGE)) {
            ConfigurationSection langSection = config.getConfigurationSection("formats." + language);
            if (langSection != null) {
                for (String key : langSection.getKeys(false)) {
                    String value = langSection.getString(key, "");
                    formats.put(key, value);
                }
            }
        }
    }

    /**
     * Get a format string by key.
     * Returns the localized version if available, otherwise falls back to en_US.
     *
     * @param key The format key (e.g., "damaged-prefix", "player-head-suffix")
     * @return The format string, or empty string if not found
     */
    public static String get(String key) {
        // Try localized format first
        if (formats.containsKey(key)) {
            return formats.get(key);
        }
        // Fall back to default (en_US)
        return defaultFormats.getOrDefault(key, "");
    }

    /**
     * Gets the primary language from SignShop config.
     * Handles the case where multiple languages are configured (comma-separated).
     */
    private static String getPrimaryLanguage() {
        try {
            SignShopConfig config = SignShop.getInstance().getSignShopConfig();
            if (config != null) {
                String languages = config.getLanguages();
                if (languages != null && !languages.isEmpty()) {
                    // Get the first language in the list
                    String primary = languages.split(",")[0].trim();
                    // Handle legacy spelling
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
     * Load hardcoded defaults as fallback if meta.yml is missing.
     */
    private static void loadHardcodedDefaults() {
        defaultFormats.put("damaged-prefix", " Damaged ");
        defaultFormats.put("colored-suffix", " Colored ");
        defaultFormats.put("player-head-suffix", "'s Head");
        defaultFormats.put("custom-head", "Custom Player Head");
        defaultFormats.put("empty-container", "Empty");
        defaultFormats.put("firework-duration", "Duration : ");
        defaultFormats.put("firework-with", " with");
        defaultFormats.put("firework-colors", " colors: ");
        defaultFormats.put("firework-fadecolors", " and fadecolors: ");
        defaultFormats.put("firework-twinkle", " +twinkle");
        defaultFormats.put("firework-trail", " +trail");
        defaultFormats.put("firework-type-ball", "Ball");
        defaultFormats.put("firework-type-ball_large", "Large Ball");
        defaultFormats.put("firework-type-star", "Star");
        defaultFormats.put("firework-type-burst", "Burst");
        defaultFormats.put("firework-type-creeper", "Creeper");
        // Potion types
        defaultFormats.put("potion-type-water", "Water");
        defaultFormats.put("potion-type-mundane", "Mundane");
        defaultFormats.put("potion-type-thick", "Thick");
        defaultFormats.put("potion-type-awkward", "Awkward");
        defaultFormats.put("potion-type-night_vision", "Night Vision");
        defaultFormats.put("potion-type-long_night_vision", "Night Vision");
        defaultFormats.put("potion-type-invisibility", "Invisibility");
        defaultFormats.put("potion-type-long_invisibility", "Invisibility");
        defaultFormats.put("potion-type-leaping", "Leaping");
        defaultFormats.put("potion-type-long_leaping", "Leaping");
        defaultFormats.put("potion-type-strong_leaping", "Leaping");
        defaultFormats.put("potion-type-fire_resistance", "Fire Resistance");
        defaultFormats.put("potion-type-long_fire_resistance", "Fire Resistance");
        defaultFormats.put("potion-type-swiftness", "Swiftness");
        defaultFormats.put("potion-type-long_swiftness", "Swiftness");
        defaultFormats.put("potion-type-strong_swiftness", "Swiftness");
        defaultFormats.put("potion-type-slowness", "Slowness");
        defaultFormats.put("potion-type-long_slowness", "Slowness");
        defaultFormats.put("potion-type-strong_slowness", "Slowness");
        defaultFormats.put("potion-type-water_breathing", "Water Breathing");
        defaultFormats.put("potion-type-long_water_breathing", "Water Breathing");
        defaultFormats.put("potion-type-healing", "Healing");
        defaultFormats.put("potion-type-strong_healing", "Healing");
        defaultFormats.put("potion-type-harming", "Harming");
        defaultFormats.put("potion-type-strong_harming", "Harming");
        defaultFormats.put("potion-type-poison", "Poison");
        defaultFormats.put("potion-type-long_poison", "Poison");
        defaultFormats.put("potion-type-strong_poison", "Poison");
        defaultFormats.put("potion-type-regeneration", "Regeneration");
        defaultFormats.put("potion-type-long_regeneration", "Regeneration");
        defaultFormats.put("potion-type-strong_regeneration", "Regeneration");
        defaultFormats.put("potion-type-strength", "Strength");
        defaultFormats.put("potion-type-long_strength", "Strength");
        defaultFormats.put("potion-type-strong_strength", "Strength");
        defaultFormats.put("potion-type-weakness", "Weakness");
        defaultFormats.put("potion-type-long_weakness", "Weakness");
        defaultFormats.put("potion-type-luck", "Luck");
        defaultFormats.put("potion-type-turtle_master", "Turtle Master");
        defaultFormats.put("potion-type-long_turtle_master", "Turtle Master");
        defaultFormats.put("potion-type-strong_turtle_master", "Turtle Master");
        defaultFormats.put("potion-type-slow_falling", "Slow Falling");
        defaultFormats.put("potion-type-long_slow_falling", "Slow Falling");
        defaultFormats.put("potion-type-wind_charged", "Wind Charged");
        defaultFormats.put("potion-type-weaving", "Weaving");
        defaultFormats.put("potion-type-oozing", "Oozing");
        defaultFormats.put("potion-type-infested", "Infested");
        // Potion effects
        defaultFormats.put("potion-effect-speed", "Speed");
        defaultFormats.put("potion-effect-slowness", "Slowness");
        defaultFormats.put("potion-effect-haste", "Haste");
        defaultFormats.put("potion-effect-mining_fatigue", "Mining Fatigue");
        defaultFormats.put("potion-effect-strength", "Strength");
        defaultFormats.put("potion-effect-instant_health", "Instant Health");
        defaultFormats.put("potion-effect-instant_damage", "Instant Damage");
        defaultFormats.put("potion-effect-jump_boost", "Jump Boost");
        defaultFormats.put("potion-effect-nausea", "Nausea");
        defaultFormats.put("potion-effect-regeneration", "Regeneration");
        defaultFormats.put("potion-effect-resistance", "Resistance");
        defaultFormats.put("potion-effect-fire_resistance", "Fire Resistance");
        defaultFormats.put("potion-effect-water_breathing", "Water Breathing");
        defaultFormats.put("potion-effect-invisibility", "Invisibility");
        defaultFormats.put("potion-effect-blindness", "Blindness");
        defaultFormats.put("potion-effect-night_vision", "Night Vision");
        defaultFormats.put("potion-effect-hunger", "Hunger");
        defaultFormats.put("potion-effect-weakness", "Weakness");
        defaultFormats.put("potion-effect-poison", "Poison");
        defaultFormats.put("potion-effect-wither", "Wither");
        defaultFormats.put("potion-effect-health_boost", "Health Boost");
        defaultFormats.put("potion-effect-absorption", "Absorption");
        defaultFormats.put("potion-effect-saturation", "Saturation");
        defaultFormats.put("potion-effect-glowing", "Glowing");
        defaultFormats.put("potion-effect-levitation", "Levitation");
        defaultFormats.put("potion-effect-luck", "Luck");
        defaultFormats.put("potion-effect-unluck", "Bad Luck");
        defaultFormats.put("potion-effect-slow_falling", "Slow Falling");
        defaultFormats.put("potion-effect-conduit_power", "Conduit Power");
        defaultFormats.put("potion-effect-dolphins_grace", "Dolphin's Grace");
        defaultFormats.put("potion-effect-bad_omen", "Bad Omen");
        defaultFormats.put("potion-effect-hero_of_the_village", "Hero of the Village");
        defaultFormats.put("potion-effect-darkness", "Darkness");
        defaultFormats.put("potion-effect-trial_omen", "Trial Omen");
        defaultFormats.put("potion-effect-raid_omen", "Raid Omen");
        defaultFormats.put("potion-effect-wind_charged", "Wind Charged");
        defaultFormats.put("potion-effect-weaving", "Weaving");
        defaultFormats.put("potion-effect-oozing", "Oozing");
        defaultFormats.put("potion-effect-infested", "Infested");
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
            InputStream in = MetaFormats.class.getResourceAsStream("/" + filename);
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
}

package org.wargamer2010.signshop.data.serialization;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.incompatibility.IncompatibilityChecker;
import org.wargamer2010.signshop.incompatibility.IncompatibilityType;
import org.wargamer2010.signshop.util.itemUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Level;

/**
 * Modern item serialization system for SignShop v5.1.0+
 *
 * <p>This class provides a robust, version-stable serialization system for storing ItemStacks
 * in SignShop's sellers.yml configuration file. It replaces the legacy Java object serialization
 * approach which suffered from cross-version compatibility issues and class versioning problems.</p>
 *
 * <h2>Architecture</h2>
 * <pre>
 * Serialization:   ItemStack → Bukkit API → YAML → Base64 → String
 * Deserialization: String → Base64 → YAML → Bukkit API → ItemStack
 * </pre>
 *
 * <h2>Why this approach</h2>
 * <ul>
 *   <li><b>Bukkit API:</b> Version-stable serialization using {@link ItemStack#serialize()},
 *       handles all item types correctly including custom items, enchantments, lore, and PDC</li>
 *   <li><b>YAML:</b> Native Bukkit format via {@link YamlConfiguration}, human-readable,
 *       preserves complex nested data structures</li>
 *   <li><b>Base64:</b> Single-line encoding compatible with sellers.yml format, prevents
 *       YAML structure conflicts</li>
 * </ul>
 *
 * <h2>Format Prefixes</h2>
 * <ul>
 *   <li><code>YAML:</code> - Modern format (v5.1.0+), preferred for all compatible items</li>
 *   <li><code>LEGACY:</code> - Explicit legacy format marker for backward compatibility</li>
 *   <li><i>No prefix</i> - Assumed legacy format from pre-5.1.0 versions</li>
 * </ul>
 *
 * <h2>Data Version Normalization (v5.2.0+)</h2>
 * <p>When Minecraft updates (e.g., 1.21.10 → 1.21.11), Bukkit's internal data version changes.
 * Items stored before the update retain their old data version, causing {@link ItemStack#equals(Object)}
 * to return false when comparing with items from player inventories. This breaks Trade shops and
 * stock checking because {@link java.util.HashMap} lookups fail.</p>
 *
 * <p><b>Solution:</b> After deserializing items (both YAML and legacy), this class performs a
 * round-trip through {@link ItemStack#serialize()} and {@link ItemStack#deserialize(Map)} to
 * normalize the data version to the current Minecraft version.</p>
 *
 * <h2>Incompatibility Detection (v5.2.0+)</h2>
 * <p>Some items serialize successfully but fail to deserialize later (e.g., player heads with
 * empty names cause {@link NullPointerException} in Spigot 1.21.10+). The serialize() method
 * proactively checks items using {@link IncompatibilityChecker} before attempting YAML serialization.
 * If incompatibilities are detected, the item is immediately serialized to LEGACY format instead.</p>
 *
 * <h2>Automatic Fallback</h2>
 * <ul>
 *   <li>If YAML serialization fails → automatic fallback to LEGACY format</li>
 *   <li>If YAML deserialization fails → automatic fallback to legacy parser</li>
 *   <li>If ItemStack deserialization fails → returns null (graceful degradation)</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>All methods are static and thread-safe. No shared mutable state exists.</p>
 *
 * <p><b>Fixes:</b> GitHub Issues #170, #168, #161, #165, #83</p>
 *
 * @author SignShop Development Team
 * @version 5.2.0
 * @since 5.1.0
 * @see ItemStack#serialize()
 * @see ItemStack#deserialize(Map)
 * @see IncompatibilityChecker
 * @see org.wargamer2010.signshop.util.DataConverter
 */
public class ItemSerializer {

    private static final String MODERN_PREFIX = "YAML:";
    private static final String LEGACY_PREFIX = "LEGACY:";

    // ========================================
    // Public API
    // ========================================

    /**
     * Serializes an ItemStack to a storage-ready string.
     *
     * <p><b>Process (v5.2.0+):</b></p>
     * <ol>
     *   <li>Check for incompatibilities using {@link IncompatibilityChecker}</li>
     *   <li>If incompatible → Use LEGACY format proactively to prevent future NPE</li>
     *   <li>If compatible → Serialize to YAML format:
     *     <ul>
     *       <li>Call {@link ItemStack#serialize()} to get Map representation</li>
     *       <li>Convert Map to YAML string via {@link YamlConfiguration}</li>
     *       <li>Encode YAML as Base64 for single-line storage</li>
     *       <li>Add "YAML:" prefix</li>
     *     </ul>
     *   </li>
     *   <li>If any error occurs → Automatic fallback to LEGACY format</li>
     * </ol>
     *
     * <p><b>Why check before serialization?</b></p>
     * <p>Some items serialize to YAML successfully but fail to deserialize later (e.g., player
     * heads with empty names in Spigot 1.21.10+ cause {@link NullPointerException} in
     * CraftMetaSkull deserialization). By checking first, we use LEGACY format proactively
     * for known problematic items, preventing shop usage failures.</p>
     *
     * @param item The ItemStack to serialize (null-safe, returns null if item is null)
     * @return Storage-ready string with "YAML:" or "LEGACY:" prefix, or null if both formats fail
     * @see #deserialize(String)
     * @see IncompatibilityChecker#checkItem(ItemStack)
     */
    public static String serialize(ItemStack item) {
        if (item == null) {
            return null;
        }

        // ==========================================
        // Phase 3A: Proactive Incompatibility Check
        // ==========================================
        // Check for known incompatibilities BEFORE attempting YAML serialization
        IncompatibilityType incompatibility = IncompatibilityChecker.checkItem(item);

        if (incompatibility != null) {
            // Item has known incompatibility - use LEGACY format proactively
            debugLog("Item has incompatibility (" + incompatibility + "), using LEGACY format for " +
                    item.getType() + " (amount: " + item.getAmount() + ")");

            try {
                String base64 = BukkitSerialization.itemStackArrayToBase64(new ItemStack[]{item});
                return LEGACY_PREFIX + base64;

            } catch (Exception e) {
                SignShop.log(
                        "Failed to serialize incompatible item to LEGACY format: " + e.getMessage(),
                        Level.SEVERE
                );
                return null;
            }
        }

        // ==========================================
        // Normal YAML Serialization (Compatible Items)
        // ==========================================
        try {
            // Step 1: Use Bukkit's serialize() to get a Map
            Map<String, Object> itemData = item.serialize();

            // Step 2: Convert Map to YAML string
            String yaml = mapToYaml(itemData);

            // Step 3: Encode YAML as Base64 for single-line storage
            String base64 = encodeBase64(yaml);

            // Step 4: Add prefix
            String result = MODERN_PREFIX + base64;

            debugLog("Serialized " + item.getType() + " (amount: " + item.getAmount() + ") to YAML format");

            return result;

        } catch (Exception e) {
            // Fallback to legacy format on any error
            return handleSerializationError(item, e);
        }
    }

    /**
     * Deserializes a string back to an ItemStack.
     *
     * <p><b>Automatic Format Detection:</b></p>
     * <ul>
     *   <li><code>"YAML:"</code> prefix → Modern format (v5.1.0+)</li>
     *   <li><code>"LEGACY:"</code> prefix → Explicit legacy format</li>
     *   <li>No prefix → Assumed legacy format from pre-5.1.0 versions</li>
     * </ul>
     *
     * <p><b>Data Version Normalization:</b></p>
     * <p>After deserializing (both formats), performs a round-trip through
     * {@link ItemStack#serialize()} and {@link ItemStack#deserialize(Map)} to normalize
     * the data version to the current Minecraft version. This ensures items stored before
     * Minecraft updates match player inventory items for Trade shops and stock checking.</p>
     *
     * <p><b>Error Handling:</b></p>
     * <ul>
     *   <li>Returns null if data is null or empty (safe for optional items)</li>
     *   <li>If modern YAML parsing fails → automatic fallback to legacy parser</li>
     *   <li>If ItemStack deserialization fails → returns null (graceful degradation)</li>
     * </ul>
     *
     * @param data The serialized string (null-safe, returns null if data is null or empty)
     * @return Deserialized ItemStack with normalized data version, or null on failure
     * @see #serialize(ItemStack)
     * @see #isModernFormat(String)
     * @see #isLegacyFormat(String)
     */
    public static ItemStack deserialize(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        try {
            // Detect format by prefix
            if (data.startsWith(MODERN_PREFIX)) {
                return deserializeModern(data);
            } else if (data.startsWith(LEGACY_PREFIX)) {
                return deserializeLegacy(data.substring(LEGACY_PREFIX.length()));
            } else {
                // No prefix = old format, assume legacy
                return deserializeLegacy(data);
            }

        } catch (Exception e) {
            return handleDeserializationError(data, e);
        }
    }

    /**
     * Checks if data is in modern YAML format.
     *
     * <p>This method is useful for migration logic and debugging to determine
     * which shops have been upgraded to the modern format.</p>
     *
     * @param data The serialized string to check (null-safe)
     * @return true if data starts with "YAML:" prefix, false otherwise (including null)
     * @see #isLegacyFormat(String)
     * @see org.wargamer2010.signshop.util.DataConverter
     */
    public static boolean isModernFormat(String data) {
        return data != null && data.startsWith(MODERN_PREFIX);
    }

    /**
     * Checks if data is in legacy format.
     *
     * <p>A string is considered legacy format if:</p>
     * <ul>
     *   <li>It starts with "LEGACY:" prefix (explicit legacy), OR</li>
     *   <li>It does NOT start with "YAML:" prefix (old data without prefix)</li>
     * </ul>
     *
     * <p>This method is useful for migration logic to identify which shops
     * need to be upgraded to the modern format.</p>
     *
     * @param data The serialized string to check (null-safe)
     * @return true if data is legacy format, false if null, empty, or modern format
     * @see #isModernFormat(String)
     * @see org.wargamer2010.signshop.util.DataConverter
     */
    public static boolean isLegacyFormat(String data) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        return data.startsWith(LEGACY_PREFIX) || !data.startsWith(MODERN_PREFIX);
    }

    // ========================================
    // Modern Format Implementation
    // ========================================

    /**
     * Deserializes modern YAML format.
     *
     * <p><b>Process:</b></p>
     * <ol>
     *   <li>Remove "YAML:" prefix and extract Base64 string</li>
     *   <li>Decode Base64 to YAML string</li>
     *   <li>Parse YAML to Map using {@link YamlConfiguration}</li>
     *   <li>Call {@link ItemStack#deserialize(Map)} to create ItemStack</li>
     *   <li>Normalize data version via round-trip serialize/deserialize</li>
     * </ol>
     *
     * <p><b>Fallback Behavior:</b></p>
     * <p>If YAML parsing fails, this method automatically attempts to deserialize
     * the Base64 string as legacy format.</p>
     *
     * @param data The serialized string starting with "YAML:" (must not be null)
     * @return Deserialized ItemStack with normalized data version, or null on failure
     */
    private static ItemStack deserializeModern(String data) {
        // Step 1: Remove prefix and get Base64 string
        String base64 = data.substring(MODERN_PREFIX.length());

        // Step 2: Decode Base64 to YAML string
        String yaml = decodeBase64(base64);

        // Step 3: Parse YAML to Map
        Map<String, Object> itemData;
        try {
            itemData = yamlToMap(yaml);
        } catch (Exception e) {
            // YAML parsing failed - fallback to legacy format
            debugLog("Modern deserialization failed, using legacy format: " + e.getMessage());
            try {
                return deserializeLegacy(base64);
            } catch (Exception legacyError) {
                SignShop.log("Both modern and legacy deserialization failed: " + legacyError.getMessage(), Level.WARNING);
                return null;
            }
        }

        // Step 4: Use Bukkit's deserialize() to create ItemStack
        try {
            ItemStack item = ItemStack.deserialize(itemData);

            // Normalize data version: Round-trip through serialize/deserialize to upgrade
            // items from old Minecraft versions (e.g., v4556 from 1.21.10) to current version
            // (e.g., v4671 in 1.21.11). This ensures items from different versions have matching
            // hashCodes for HashMap lookups in stock checking.
            // Without this, items stored before a Minecraft update won't match player inventory items.
            try {
                item = ItemStack.deserialize(item.serialize());
            } catch (Exception normalizeEx) {
                // If normalization fails, use the original item
                debugLog("Data version normalization failed, using original: " + normalizeEx.getMessage());
            }

            debugLog("Deserialized (modern): " + (item != null ? item.getType() : "null"));
            return item;
        } catch (Exception e) {
            // ItemStack deserialization failed (e.g., corrupt player heads with empty names)
            // This can happen with incompatible items that passed YAML parsing but fail during
            // Bukkit's internal deserialization (NullPointerException in CraftMetaSkull, etc.)
            SignShop.log("ItemStack deserialization failed (incompatible item data): " + e.getMessage(), Level.WARNING);
            debugLog("ItemStack.deserialize() threw exception, returning null");
            return null;
        }
    }

    // ========================================
    // Legacy Format Implementation
    // ========================================

    /**
     * Deserializes legacy BukkitObjectOutputStream format.
     *
     * <p><b>Legacy Format:</b> Uses Java object serialization via
     * {@link BukkitSerialization#itemStackArrayFromBase64(String)}.
     * This format was used in SignShop versions prior to v5.1.0.</p>
     *
     * <p><b>Special Handling:</b></p>
     * <ul>
     *   <li>Written books have {@link itemUtil#fixBookLegacy(ItemStack)} applied</li>
     *   <li>Data version normalization is performed after deserialization</li>
     * </ul>
     *
     * @param base64 The Base64-encoded serialized data (must not be null)
     * @return Deserialized ItemStack with normalized data version, or null if array is empty
     * @throws IOException if Base64 decoding or object deserialization fails
     */
    private static ItemStack deserializeLegacy(String base64) throws IOException {
        // Use old BukkitSerialization to read legacy format
        ItemStack[] items = BukkitSerialization.itemStackArrayFromBase64(base64);
        ItemStack item = (items.length > 0) ? items[0] : null;

        // Apply book fix only for legacy books
        if (item != null && item.getType() == Material.WRITTEN_BOOK) {
            itemUtil.fixBookLegacy(item);
        }

        // Normalize data version (same as modern format) to ensure cross-version compatibility
        if (item != null) {
            try {
                item = ItemStack.deserialize(item.serialize());
            } catch (Exception normalizeEx) {
                // If normalization fails, use the original item
                debugLog("Legacy data version normalization failed, using original: " + normalizeEx.getMessage());
            }
        }

        debugLog("Deserialized (legacy): " + (item != null ? item.getType() : "null"));

        return item;
    }

    // ========================================
    // YAML Conversion Helpers
    // ========================================

    /**
     * Converts a Map to YAML string using Bukkit's YamlConfiguration.
     *
     * <p>This ensures proper handling of all Bukkit-specific types
     * (Color, Enchantment, PotionEffect, etc.).</p>
     *
     * @param map The Map representation from {@link ItemStack#serialize()}
     * @return YAML string representation of the item
     */
    private static String mapToYaml(Map<String, Object> map) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("item", map);
        return yaml.saveToString();
    }

    /**
     * Converts YAML string back to Map using Bukkit's YamlConfiguration.
     *
     * <p><b>Pre-validation:</b> Checks for known corrupt data patterns
     * (e.g., empty player head names that cause NPE in Spigot 1.21.10+).</p>
     *
     * @param yaml The YAML string to parse
     * @return Map representation suitable for {@link ItemStack#deserialize(Map)}
     * @throws RuntimeException if YAML parsing fails or corrupt data detected
     */
    private static Map<String, Object> yamlToMap(String yaml) {
        // Pre-validate: Detect corrupt player heads with empty names (causes NPE in Spigot 1.21)
        // This prevents Bukkit from logging scary stacktraces for known corrupt data
        if (yaml.contains("meta-type: SKULL") && yaml.contains("name: ''")) {
            // Corrupt player head with empty name - will cause NPE in CraftMetaSkull deserialization
            throw new RuntimeException("Corrupt player head detected (empty name) - cannot deserialize");
        }

        YamlConfiguration config = new YamlConfiguration();

        try {
            config.loadFromString(yaml);
        } catch (Exception e) {
            throw new RuntimeException("YAML parsing failed: " + e.getMessage(), e);
        }

        // Get the ConfigurationSection and convert to Map
        Object itemSection = config.get("item");

        if (itemSection == null) {
            throw new RuntimeException("No 'item' key found in YAML");
        }

        // ConfigurationSection needs to be converted to Map
        if (itemSection instanceof org.bukkit.configuration.ConfigurationSection) {
            org.bukkit.configuration.ConfigurationSection section =
                    (org.bukkit.configuration.ConfigurationSection) itemSection;
            return section.getValues(false);
        }

        // If it's already a Map (shouldn't happen, but safe fallback)
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) itemSection;

        return map;
    }

    // ========================================
    // Base64 Encoding Helpers
    // ========================================

    /**
     * Encodes a string to Base64 for single-line storage.
     *
     * <p>This is necessary because sellers.yml format expects single-line values.
     * Multi-line YAML strings would break the configuration file structure.</p>
     *
     * @param str The string to encode
     * @return Base64-encoded string
     */
    private static String encodeBase64(String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Decodes Base64 string back to original string.
     *
     * @param base64 The Base64-encoded string to decode
     * @return Decoded string
     * @throws RuntimeException if Base64 decoding fails (invalid format)
     */
    private static String decodeBase64(String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Base64 decoding failed: " + e.getMessage(), e);
        }
    }

    // ========================================
    // Error Handling
    // ========================================

    /**
     * Handles serialization errors by falling back to legacy format.
     *
     * <p>Called when modern YAML serialization fails. Attempts to serialize
     * using the legacy BukkitObjectOutputStream format as a last resort.</p>
     *
     * @param item The ItemStack that failed to serialize
     * @param e The exception that caused serialization to fail
     * @return LEGACY-prefixed serialized string, or null if fallback also fails
     */
    private static String handleSerializationError(ItemStack item, Exception e) {
        SignShop.log(
                "Modern serialization failed for " + item.getType() +
                        ", falling back to legacy: " + e.getMessage(),
                Level.WARNING
        );

        try {
            return LEGACY_PREFIX + BukkitSerialization.itemStackArrayToBase64(new ItemStack[]{item});
        } catch (Exception fallbackError) {
            SignShop.log(
                    "Legacy serialization also failed: " + fallbackError.getMessage(),
                    Level.SEVERE
            );
            return null;
        }
    }

    /**
     * Handles deserialization errors with helpful logging.
     *
     * <p>Logs the error and, if debugging is enabled, logs a preview of the
     * problematic data for troubleshooting.</p>
     *
     * @param data The data string that failed to deserialize
     * @param e The exception that caused deserialization to fail
     * @return Always returns null (deserialization failed)
     */
    private static ItemStack handleDeserializationError(String data, Exception e) {
        SignShop.log("Failed to deserialize item: " + e.getMessage(), Level.WARNING);

        if (SignShop.getInstance().getSignShopConfig().debugging()) {
            String preview = (data.length() > 100) ?
                    data.substring(0, 100) + "..." :
                    data;
            SignShop.log("Problematic data: " + preview, Level.WARNING);
        }

        return null;
    }

    // ========================================
    // Debug Logging
    // ========================================

    /**
     * Logs debug messages when debugging is enabled.
     *
     * <p>Messages are only logged if debugging is enabled in SignShopConfig
     * and "ItemSerializer" is in the debug classes list (or debugging is global).</p>
     *
     * @param message The debug message to log
     */
    private static void debugLog(String message) {
        SignShop.getInstance().debugClassMessage(message, "ItemSerializer");
    }
}
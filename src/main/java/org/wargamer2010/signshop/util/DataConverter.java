package org.wargamer2010.signshop.util;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.util.FileUtil;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.data.*;
import org.wargamer2010.signshop.data.serialization.BukkitSerialization;
import org.wargamer2010.signshop.incompatibility.IncompatibilityChecker;
import org.wargamer2010.signshop.incompatibility.IncompatibilityType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Handles data migration for SignShop shops from old formats to modern formats.
 *
 * <p><b>Format Detection (Smart Migration):</b></p>
 * <ol>
 *   <li><b>YAML:</b> prefix - Modern format (v5.1.0+), skip migration</li>
 *   <li><b>LEGACY:</b> or rO0AB prefix - DataVersion 3 legacy, upgrade to YAML if compatible</li>
 *   <li><b>No prefix</b> - Very old format (pre-DataVersion 3), full conversion if compatible</li>
 * </ol>
 *
 * <p><b>Incompatibility Handling:</b></p>
 * <p>Before migrating items to YAML format, checks for incompatibilities using the
 * {@link IncompatibilityChecker}. If incompatible items are detected:
 * <ul>
 *   <li>Shop stays in LEGACY format (safe, won't cause NPE)</li>
 *   <li>Detailed warnings logged with issue type and solution</li>
 *   <li>Shop remains functional in legacy format</li>
 * </ul>
 *
 * <p><b>Automatic Re-Migration (New in v5.2.0):</b></p>
 * <p>Even after initial migration completes, DataConverter continues to monitor shops
 * kept in LEGACY format. On each server startup:
 * <ol>
 *   <li>Checks if incompatibility detectors are still relevant for current Spigot version</li>
 *   <li>If ALL detectors report issues are fixed → Re-attempts migration</li>
 *   <li>Successfully re-migrates shops from LEGACY to YAML automatically</li>
 * </ol>
 *
 * <p><b>Example Scenario:</b></p>
 * <pre>
 * Server with Spigot 1.21.10:
 *   - Shop has custom heads with empty names
 *   - Incompatibility detected → Shop stays LEGACY
 *   - DataVersion = current
 *
 * Server updates to Spigot 1.21.20 (hypothetical fix):
 *   - DataVersion already current → Skips full conversion
 *   - Detectors check: PlayerHeadIncompatibilityDetector.isRelevantForCurrentVersion() = false
 *   - Re-checks LEGACY shops → No incompatibilities found!
 *   - Shop automatically migrates to YAML ✓
 * </pre>
 *
 * <p>This ensures shops don't stay in LEGACY format forever when underlying issues are resolved.</p>
 *
 * @author SignShop Development Team
 * @version 5.2.0
 * @since 5.0.0
 */
public class DataConverter {
    static File sellersFile;
    static File sellersFileBackup;
    static File timingFile;
    static File timingFileBackup;

    /**
     * Initializes and executes the data conversion process.
     *
     * <p><b>Process Flow:</b></p>
     * <ol>
     *   <li>Loads sellers.yml configuration</li>
     *   <li>Compares DataVersion against current version</li>
     *   <li>If outdated → Creates backup, then runs {@link #convertData(FileConfiguration)}
     *       and {@link #convertTiming()}</li>
     *   <li>If current → Runs {@link #recheckLegacyShops(FileConfiguration)} for auto-migration</li>
     * </ol>
     *
     * <p><b>Backup Strategy:</b> Creates timestamped backup before ANY modification.</p>
     *
     * @see #convertData(FileConfiguration)
     * @see #recheckLegacyShops(FileConfiguration)
     */
    public static void init() {
        File dataFolder = SignShop.getInstance().getDataFolder();
        sellersFile = new File(dataFolder, "sellers.yml");
        FileConfiguration sellers = new YamlConfiguration();
        try {
            sellers.load(sellersFile);
            SignShop.log("Checking data version.", Level.INFO);
            if (sellers.getInt("DataVersion") < SignShop.DATA_VERSION) {
                sellersFileBackup = new File(dataFolder, "sellersBackup" + SSTimeUtil.getDateTimeStamp() + ".yml");
                FileUtil.copy(sellersFile, sellersFileBackup);
                convertData(sellers);
                convertTiming();
            } else {
                SignShop.log("Your data is current.", Level.INFO);

                // Even if data is current, re-check shops kept in LEGACY format
                // This allows automatic migration when Bukkit fixes incompatibility issues
                recheckLegacyShops(sellers);
            }
        } catch (IOException | InvalidConfigurationException ignored) {
        }
    }

    /**
     * Converts timing.yml data from old plugin name references.
     *
     * <p>Renames "sshotel" expirable keys to "signshophotel" for consistency
     * with modern plugin naming. Creates backup before modifications.</p>
     */
    private static void convertTiming() {
        File dataFolder = SignShop.getInstance().getDataFolder();
        timingFile = new File(dataFolder, "timing.yml");
        FileConfiguration timing = new YamlConfiguration();
        try {
            timing.load(timingFile);
            ConfigurationSection expirables = timing.getConfigurationSection("expirables");
            if (expirables != null && !expirables.getKeys(false).isEmpty()) {
                timingFileBackup = new File(dataFolder, "timingBackup" + SSTimeUtil.getDateTimeStamp() + ".yml");
                FileUtil.copy(timingFile, timingFileBackup);
                for (String key : expirables.getKeys(false)) {
                    if (key.contains("sshotel")) {
                        Map<String, Object> propertyMap = expirables.getConfigurationSection(key).getValues(true);
                        timing.createSection("expirables." + key.replace("sshotel", "signshophotel"), propertyMap);
                    }
                }
                timing.save(timingFile);
            }
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Re-checks shops that were kept in LEGACY format due to incompatibilities.
     *
     * <p><b>Purpose:</b> Allows automatic migration when Bukkit fixes incompatibility issues.
     * Without this, shops would stay in LEGACY format forever even after the bug is fixed.</p>
     *
     * <p><b>When It Runs:</b></p>
     * <ul>
     *   <li>Every server startup (after DataVersion check passes)</li>
     *   <li>Only affects shops with LEGACY: prefixed items/chests</li>
     *   <li>Only attempts migration if detectors say issues are fixed</li>
     * </ul>
     *
     * <p><b>How It Works:</b></p>
     * <ol>
     *   <li>Check if any incompatibility detectors are still relevant</li>
     *   <li>If NO detectors are relevant → All known issues are fixed!</li>
     *   <li>Scan for shops with LEGACY format data</li>
     *   <li>Re-attempt migration to YAML for those shops</li>
     *   <li>Save if any shops were migrated</li>
     * </ol>
     *
     * <p><b>Performance:</b> Fast - only processes shops with LEGACY format, which should be rare.</p>
     *
     * @param sellers Configuration containing shop data
     */
    private static void recheckLegacyShops(FileConfiguration sellers) {
        try {
            // First, check if any incompatibility detectors are still relevant
            int relevantDetectors = IncompatibilityChecker.getRelevantDetectorCount();

            if (relevantDetectors > 0) {
                // Issues still exist in current Spigot version, don't re-check
                SignShop.log("Incompatibility detectors still active (" + relevantDetectors + " active). " +
                        "Shops in LEGACY format will not be re-checked.", Level.FINE);
                return;
            }

            // All detectors report issues are fixed! Re-check LEGACY shops
            SignShop.log("All known incompatibility issues are fixed in this server version. " +
                    "Re-checking shops kept in LEGACY format...", Level.INFO);

            ConfigurationSection section = sellers.getConfigurationSection("sellers");
            if (section == null) {
                return;
            }

            Set<String> shops = section.getKeys(false);
            int shopsRemigrated = 0;
            int chestsRemigrated = 0;
            boolean needsSave = false;

            for (String shop : shops) {
                StringBuilder itemPath = new StringBuilder().append("sellers.").append(shop).append(".items");
                StringBuilder miscPath = new StringBuilder().append("sellers.").append(shop).append(".misc");

                // ==========================================
                // Check for LEGACY format items
                // ==========================================
                List<String> items = sellers.getStringList(itemPath.toString());

                if (!items.isEmpty()) {
                    String firstItem = items.getFirst();

                    // Only process if in LEGACY format
                    if (firstItem.startsWith("LEGACY:")) {
                        try {
                            SignShop.log("Re-checking shop " + shop + " (was kept in LEGACY format)", Level.INFO);

                            // Deserialize from legacy
                            ItemStack[] legacyItems = new ItemStack[items.size()];
                            for (int i = 0; i < items.size(); i++) {
                                String itemData = items.get(i);
                                if (itemData.startsWith("LEGACY:")) {
                                    itemData = itemData.substring("LEGACY:".length());
                                }

                                ItemStack[] decoded = BukkitSerialization.itemStackArrayFromBase64(itemData);
                                if (decoded != null && decoded.length > 0) {
                                    legacyItems[i] = decoded[0];
                                }
                            }

                            // Check if STILL incompatible (shouldn't be, but be safe)
                            if (IncompatibilityChecker.hasIncompatibleItems(legacyItems)) {
                                // Still incompatible? This shouldn't happen since detectors say issues are fixed
                                SignShop.log("Shop " + shop + " still has incompatible items despite detectors being inactive. " +
                                        "This may indicate a new incompatibility type. Keeping in LEGACY format.", Level.WARNING);
                            } else {
                                // Compatible now! Migrate to YAML
                                sellers.set(itemPath.toString(), itemUtil.convertItemStacksToString(legacyItems));
                                shopsRemigrated++;
                                needsSave = true;

                                SignShop.log("✓ Shop " + shop + " successfully migrated from LEGACY to YAML format", Level.INFO);
                            }

                        } catch (Exception e) {
                            SignShop.log("Failed to re-check shop " + shop + ": " + e.getMessage(), Level.WARNING);
                        }
                    }
                }

                // ==========================================
                // Check for LEGACY format chests
                // ==========================================
                List<String> misc = sellers.getStringList(miscPath.toString());

                for (int i = 0; i < misc.size(); i++) {
                    String miscEntry = misc.get(i);

                    // Check if this is a chest entry with LEGACY format
                    if ((miscEntry.startsWith("chest1:LEGACY:") || miscEntry.startsWith("chest2:LEGACY:"))) {
                        try {
                            String[] parts = miscEntry.split(":", 2);
                            String key = parts[0];
                            String value = parts[1];

                            SignShop.log("Re-checking " + key + " for shop " + shop + " (was kept in LEGACY format)", Level.INFO);

                            // Parse LEGACY format chest data
                            String[] legacyItems = value.split("~");
                            List<ItemStack> chestItems = new ArrayList<>();

                            for (String legacyItem : legacyItems) {
                                if (legacyItem.startsWith("LEGACY:")) {
                                    String base64 = legacyItem.substring("LEGACY:".length());
                                    ItemStack[] decoded = BukkitSerialization.itemStackArrayFromBase64(base64);
                                    if (decoded != null && decoded.length > 0 && decoded[0] != null) {
                                        chestItems.add(decoded[0]);
                                    }
                                }
                            }

                            if (!chestItems.isEmpty()) {
                                ItemStack[] chestArray = chestItems.toArray(new ItemStack[0]);

                                // Check if STILL incompatible
                                if (IncompatibilityChecker.hasIncompatibleItems(chestArray)) {
                                    SignShop.log(key + " for shop " + shop + " still has incompatible items. Keeping in LEGACY format.", Level.WARNING);
                                } else {
                                    // Compatible now! Migrate to YAML
                                    List<String> modernItems = new ArrayList<>();
                                    for (ItemStack item : chestArray) {
                                        if (item != null) {
                                            String[] modernItem = itemUtil.convertItemStacksToString(new ItemStack[]{item});
                                            if (modernItem.length > 0) {
                                                modernItems.add(modernItem[0]);
                                            }
                                        }
                                    }

                                    if (!modernItems.isEmpty()) {
                                        String newEntry = key + ":" + String.join("~", modernItems);
                                        misc.set(i, newEntry);
                                        chestsRemigrated++;
                                        needsSave = true;

                                        SignShop.log("✓ " + key + " for shop " + shop + " successfully migrated from LEGACY to YAML format", Level.INFO);
                                    }
                                }
                            }

                        } catch (Exception e) {
                            SignShop.log("Failed to re-check chest for shop " + shop + ": " + e.getMessage(), Level.WARNING);
                        }
                    }
                }

                // Save updated misc if chests were migrated
                if (needsSave && !misc.isEmpty()) {
                    sellers.set(miscPath.toString(), misc);
                }
            }

            // Save if anything was migrated
            if (needsSave) {
                sellers.save(sellersFile);

                SignShop.log("=============================================================", Level.INFO);
                SignShop.log("LEGACY Re-Migration Complete!", Level.INFO);
                SignShop.log("  - Shops re-migrated to YAML: " + shopsRemigrated, Level.INFO);
                SignShop.log("  - Chests re-migrated to YAML: " + chestsRemigrated, Level.INFO);
                SignShop.log("All shops are now using modern YAML format.", Level.INFO);
                SignShop.log("=============================================================", Level.INFO);
            } else {
                SignShop.log("No shops needed re-migration (none were in LEGACY format).", Level.FINE);
            }

        } catch (Exception e) {
            SignShop.log("Error during LEGACY shop re-check: " + e.getMessage(), Level.WARNING);
            e.printStackTrace();
        }
    }

    /**
     * Converts legacy shop data to modern format with smart format detection.
     *
     * <p><b>Handles three scenarios:</b></p>
     * <ol>
     *   <li>YAML: prefix - Already modern, skip</li>
     *   <li>rO0AB or LEGACY: prefix - DataVersion 3 legacy, upgrade to YAML if compatible</li>
     *   <li>No prefix - Very old format (pre-DataVersion 3), full conversion if compatible</li>
     * </ol>
     *
     * <p><b>Incompatibility Checking:</b></p>
     * <p>Before upgrading items to YAML, checks for incompatibilities. If found,
     * shop stays in LEGACY format with detailed warnings logged.</p>
     *
     * <p>Also migrates chest1/chest2 data from legacy Base64 to YAML format.</p>
     */
    private static void convertData(FileConfiguration sellers) {
        try {
            SignShop.log("Converting old data.", Level.INFO);
            ConfigurationSection section = sellers.getConfigurationSection("sellers");
            if (section == null) {
                SignShop.log("There was a problem with the sellers.yml, attempting to fix it. If the problem persists try regenerating the SignShop folder.", Level.WARNING);
                sellers.set("sellers", new ArrayList<>());
            } else {
                Set<String> shops = section.getKeys(false);
                int itemsConverted = 0;
                int itemsSkipped = 0;
                int itemsKeptLegacy = 0;
                int chestsConverted = 0;

                for (String shop : shops) {
                    StringBuilder itemPath = new StringBuilder().append("sellers.").append(shop).append(".items");
                    StringBuilder miscPath = new StringBuilder().append("sellers.").append(shop).append(".misc");

                    // ==========================================
                    // ITEM CONVERSION - Smart format detection
                    // ==========================================
                    List<String> items = sellers.getStringList(itemPath.toString());

                    if (!items.isEmpty()) {
                        String firstItem = items.getFirst();

                        // SCENARIO 1: Already modern (YAML format)
                        if (firstItem.startsWith("YAML:")) {
                            itemsSkipped++;
                            // Skip - already in modern format

                            // SCENARIO 2: DataVersion 3 legacy format (needs upgrade to YAML)
                        } else if (firstItem.startsWith("rO0AB") || firstItem.startsWith("LEGACY:")) {
                            try {
                                SignShop.log("Upgrading DataVersion 3 items to YAML for shop: " + shop, Level.INFO);

                                // Deserialize from legacy Base64
                                ItemStack[] legacyItems = new ItemStack[items.size()];
                                for (int i = 0; i < items.size(); i++) {
                                    String itemData = items.get(i);
                                    // Remove LEGACY: prefix if present
                                    if (itemData.startsWith("LEGACY:")) {
                                        itemData = itemData.substring("LEGACY:".length());
                                    }

                                    ItemStack[] decoded = BukkitSerialization.itemStackArrayFromBase64(itemData);
                                    if (decoded != null && decoded.length > 0) {
                                        legacyItems[i] = decoded[0];
                                    }
                                }

                                // *** INCOMPATIBILITY CHECK ***
                                // Check for incompatible items BEFORE migrating to YAML
                                if (IncompatibilityChecker.hasIncompatibleItems(legacyItems)) {
                                    // Found incompatible items - keep in legacy format
                                    List<IncompatibilityType> issues = IncompatibilityChecker.checkAll(legacyItems);
                                    logIncompatibilityWarning(shop, issues);
                                    itemsKeptLegacy++;
                                    // Don't modify items - keep original legacy format
                                } else {
                                    // Safe to migrate to YAML - no incompatibilities found
                                    sellers.set(itemPath.toString(), itemUtil.convertItemStacksToString(legacyItems));
                                    itemsConverted++;
                                }

                            } catch (Exception e) {
                                SignShop.log("Failed to upgrade DataVersion 3 items for shop " + shop + ": " + e.getMessage(), Level.WARNING);
                                SignShop.log("Keeping original format", Level.WARNING);
                            }

                            // SCENARIO 3: Very old format (pre-DataVersion 3) - needs full conversion
                        } else {
                            try {
                                SignShop.log("Converting very old format items for shop: " + shop, Level.INFO);

                                // Use the old conversion method
                                ItemStack[] convertedItems = convertOldStringsToItemStacks(items);

                                // *** INCOMPATIBILITY CHECK ***
                                // Check for incompatible items BEFORE migrating to YAML
                                if (IncompatibilityChecker.hasIncompatibleItems(convertedItems)) {
                                    // Found incompatible items - keep in original format
                                    List<IncompatibilityType> issues = IncompatibilityChecker.checkAll(convertedItems);
                                    logIncompatibilityWarning(shop, issues);
                                    itemsKeptLegacy++;
                                    // Don't modify items - keep original format
                                } else {
                                    // Safe to migrate to YAML - no incompatibilities found
                                    sellers.set(itemPath.toString(), itemUtil.convertItemStacksToString(convertedItems));
                                    itemsConverted++;
                                }

                            } catch (Exception e) {
                                SignShop.log("Failed to convert very old format items for shop " + shop + ": " + e.getMessage(), Level.WARNING);
                                SignShop.log("Keeping original format for safety", Level.WARNING);
                            }
                        }
                    }

                    // ==========================================
                    // MISC CONVERSION - Strip old data + migrate chests
                    // ==========================================
                    List<String> misc = sellers.getStringList(miscPath.toString());
                    if (!misc.isEmpty()) {
                        List<String> newMisc = new ArrayList<>();

                        for (String miscString : misc) {
                            String[] keyPair = miscString.split(":", 2);
                            String key = keyPair.length >= 1 ? keyPair[0] : "";
                            String data = keyPair.length == 2 ? keyPair[1] : "";

                            // Strip old pipe-separated data if it exists
                            if (data.contains("|")) {
                                String[] dataPair = data.split("\\|", 2);
                                data = dataPair.length == 2 ? dataPair[1] : dataPair[0];
                            }

                            newMisc.add(key + ":" + data);
                        }

                        // Migrate chest serialization (chest1, chest2) with incompatibility detection
                        List<String> migratedMisc = migrateChestSerialization(newMisc, shop);
                        if (migratedMisc.size() > newMisc.size() || !migratedMisc.equals(newMisc)) {
                            chestsConverted++;
                        }

                        sellers.set(miscPath.toString(), migratedMisc);
                    }
                }

                SignShop.log("Data conversion of " + shops.size() + " shops has finished.", Level.INFO);
                SignShop.log("  - Items upgraded to YAML: " + itemsConverted, Level.INFO);
                SignShop.log("  - Items already modern (skipped): " + itemsSkipped, Level.INFO);
                SignShop.log("  - Items kept in LEGACY (incompatibilities): " + itemsKeptLegacy, Level.INFO);
                SignShop.log("  - Shops with chests migrated: " + chestsConverted, Level.INFO);
            }

            sellers.set("DataVersion", SignShop.DATA_VERSION);
            sellers.save(sellersFile);

        } catch (IOException e) {
            SignShop.log("Error converting data!", Level.WARNING);
            e.printStackTrace();
        }
    }

    /**
     * Logs detailed warning about incompatible items found in a shop.
     *
     * <p>Provides user-friendly information about:
     * <ul>
     *   <li>What incompatibility was found</li>
     *   <li>Why it's incompatible</li>
     *   <li>How to fix it</li>
     * </ul>
     *
     * @param shopId Shop identifier for logging
     * @param issues List of incompatibility types found
     */
    private static void logIncompatibilityWarning(String shopId, List<IncompatibilityType> issues) {
        SignShop.log("=============================================================", Level.WARNING);
        SignShop.log("Shop " + shopId + " kept in LEGACY format (incompatible items detected)", Level.WARNING);

        // Count occurrences of each issue type
        Map<IncompatibilityType, Integer> issueCounts = new HashMap<>();
        for (IncompatibilityType issue : issues) {
            issueCounts.put(issue, issueCounts.getOrDefault(issue, 0) + 1);
        }

        // Log each unique issue type with count
        for (Map.Entry<IncompatibilityType, Integer> entry : issueCounts.entrySet()) {
            IncompatibilityType issue = entry.getKey();
            int count = entry.getValue();

            SignShop.log("  Issue: " + issue.getDescription() + " (x" + count + ")", Level.WARNING);
            SignShop.log("  Affected Versions: " + issue.getAffectedVersions(), Level.WARNING);
            SignShop.log("  Solution: " + issue.getSolution(), Level.WARNING);
        }

        SignShop.log("The shop will continue to work in legacy format.", Level.WARNING);
        SignShop.log("=============================================================", Level.WARNING);
    }

    /**
     * Migrates legacy chest data (chest1:, chest2:) from BukkitObjectOutputStream
     * to modern YAML format. Part of v5.1.0 modernization.
     *
     * <p>Handles multi-line Base64 where a single item's Base64 data wraps across lines,
     * and multiple items are separated by lines starting with ~.</p>
     *
     * <p><b>Incompatibility Handling:</b></p>
     * <p>Before migrating chest contents to YAML, checks for incompatibilities.
     * If found, chest stays in LEGACY format.</p>
     *
     * @param miscList List of misc data strings
     * @param shopName Shop identifier for logging
     * @return Updated list with modern serialization
     */
    private static List<String> migrateChestSerialization(List<String> miscList, String shopName) {
        if (miscList == null || miscList.isEmpty()) {
            return miscList;
        }

        List<String> updated = new ArrayList<>();
        int migratedCount = 0;

        for (String miscEntry : miscList) {
            // Parse key:value format
            String[] parts = miscEntry.split(":", 2);
            if (parts.length != 2) {
                updated.add(miscEntry);
                continue;
            }

            String key = parts[0];
            String value = parts[1];

            // Only migrate chest1 and chest2 entries
            if (!key.equals("chest1") && !key.equals("chest2")) {
                updated.add(miscEntry);
                continue;
            }

            // Check if already modern format (no newlines, just ~ separators)
            if (value.startsWith("YAML:") && !value.contains("\n")) {
                updated.add(miscEntry);
                continue;
            }

            // Check if it's the separator character (chest might be empty)
            if (value.equals(Storage.getItemSeperator()) || value.isEmpty()) {
                updated.add(miscEntry);
                continue;
            }

            try {
                List<String> modernItems = new ArrayList<>();
                List<ItemStack> chestItems = new ArrayList<>();

                // Split by lines that START with ~ (item boundaries)
                String[] itemChunks = miscEntry.split("\n(?=~)");

                // ==========================================
                // FIRST PASS: Deserialize all items
                // ==========================================
                for (int i = 0; i < itemChunks.length; i++) {
                    String chunk = itemChunks[i];
                    String base64Data;

                    if (i == 0) {
                        // First chunk: "chest1:rO0AB...\n  continues...\n  more..."
                        String[] firstParts = chunk.split(":", 2);
                        if (firstParts.length != 2) {
                            // Malformed, keep original
                            updated.add(miscEntry);
                            break;
                        }
                        base64Data = firstParts[1].replaceAll("\\s+", "");
                    } else {
                        // Continuation chunks: "~rO0AB...\n  continues...\n  more..."
                        if (!chunk.startsWith("~")) continue;
                        base64Data = chunk.substring(1).replaceAll("\\s+", "");
                    }

                    // Skip if already modern
                    if (base64Data.startsWith("YAML:")) {
                        base64Data = base64Data.substring("YAML:".length());
                    } else if (base64Data.startsWith("LEGACY:")) {
                        base64Data = base64Data.substring("LEGACY:".length());
                    }

                    // Deserialize from legacy format
                    try {
                        ItemStack[] items = BukkitSerialization.itemStackArrayFromBase64(base64Data);
                        if (items != null && items.length > 0 && items[0] != null) {
                            chestItems.add(items[0]);
                        }
                    } catch (Exception e) {
                        SignShop.log("Failed to deserialize chest item: " + e.getMessage(), Level.WARNING);
                    }
                }

                if (chestItems.isEmpty()) {
                    // No items deserialized, keep original
                    updated.add(miscEntry);
                    continue;
                }

                // ==========================================
                // INCOMPATIBILITY CHECK
                // ==========================================
                ItemStack[] chestArray = chestItems.toArray(new ItemStack[0]);

                if (IncompatibilityChecker.hasIncompatibleItems(chestArray)) {
                    // Found incompatible items - keep ENTIRE chest in LEGACY format
                    List<IncompatibilityType> issues = IncompatibilityChecker.checkAll(chestArray);

                    SignShop.log("=============================================================", Level.WARNING);
                    SignShop.log("Shop " + shopName + " " + key + " kept in LEGACY format (incompatible items)", Level.WARNING);

                    for (IncompatibilityType issue : new HashSet<>(issues)) {
                        SignShop.log("  Issue: " + issue.getDescription(), Level.WARNING);
                        SignShop.log("  Solution: " + issue.getSolution(), Level.WARNING);
                    }

                    SignShop.log("=============================================================", Level.WARNING);

                    // Re-serialize all items as LEGACY
                    List<String> legacyItems = new ArrayList<>();
                    for (ItemStack item : chestArray) {
                        if (item != null) {
                            try {
                                String legacyBase64 = BukkitSerialization.itemStackArrayToBase64(new ItemStack[]{item});
                                legacyItems.add("LEGACY:" + legacyBase64);
                            } catch (Exception e) {
                                SignShop.log("Failed to re-serialize item to legacy format: " + e.getMessage(), Level.WARNING);
                            }
                        }
                    }

                    if (!legacyItems.isEmpty()) {
                        String flatFormat = key + ":" + String.join("~", legacyItems);
                        updated.add(flatFormat);
                    } else {
                        updated.add(miscEntry);
                    }

                } else {
                    // Safe to migrate ENTIRE chest to YAML - no incompatibilities found
                    for (ItemStack item : chestArray) {
                        if (item != null) {
                            String[] modernItem = itemUtil.convertItemStacksToString(new ItemStack[]{item});
                            if (modernItem.length > 0) {
                                modernItems.add(modernItem[0]);
                            }
                        }
                    }

                    if (!modernItems.isEmpty()) {
                        String flatFormat = key + ":" + String.join("~", modernItems);
                        updated.add(flatFormat);
                        migratedCount++;
                    } else {
                        updated.add(miscEntry);
                    }
                }

            } catch (Exception e) {
                // If migration fails, keep original (better than losing data)
                SignShop.log("Failed to migrate " + key + " data for shop " + shopName + ", keeping original: " +
                        e.getMessage(), Level.WARNING);
                e.printStackTrace();
                updated.add(miscEntry);
            }
        }

        if (migratedCount > 0) {
            SignShop.log("Migrated " + migratedCount + " chest entries to modern format", Level.INFO);
        }

        return updated;
    }

    /**
     * Converts very old item format (pre-DataVersion 3) to ItemStack array.
     *
     * <p><b>Old Format:</b> {@code [amount]~[material]~[durability]~[data]~[enchants]~[bookID]~[metaID]~[base64]}</p>
     *
     * <p>If Base64 NBT data exists (property 7+), deserializes from that for highest fidelity.
     * Otherwise, reconstructs from individual properties using deprecated APIs.</p>
     *
     * @param itemStringList List of old format item strings
     * @return Array of converted ItemStacks, nulls removed
     */
    public static ItemStack[] convertOldStringsToItemStacks(List<String> itemStringList) {
        IItemTags itemTags = BookFactory.getItemTags();
        ItemStack[] itemStacks = new ItemStack[itemStringList.size()];
        int invalidItems = 0;

        for (int i = 0; i < itemStringList.size(); i++) {
            try {
                String[] itemProperties = itemStringList.get(i).split(Storage.getItemSeperator());
                if (itemProperties.length < 4) {
                    invalidItems++;
                    continue;
                }

                if (itemProperties.length <= 7) {
                    if (i < (itemStringList.size() - 1) && itemStringList.get(i + 1).split(Storage.getItemSeperator()).length < 4) {
                        // Bug detected, the next item will be the base64 string belonging to the current item
                        // This bug will be fixed at the next save as the ~ will be replaced with a |
                        itemProperties = (itemStringList.get(i) + "|" + itemStringList.get(i + 1)).split(Storage.getItemSeperator());
                    }
                }

                if (itemProperties.length > 7) {
                    String base64prop = itemProperties[7];
                    // The ~ and | are used to differentiate between the old NBTLib and the BukkitSerialization
                    if (base64prop != null && (base64prop.startsWith("~") || base64prop.startsWith("|"))) {
                        String joined = itemUtil.Join(itemProperties, 7).substring(1);

                        ItemStack[] convertedStacks = BukkitSerialization.itemStackArrayFromBase64(joined);
                        if (convertedStacks.length > 0 && convertedStacks[0] != null) {
                            itemStacks[i] = convertedStacks[0];
                        }
                    }
                }

                if (itemStacks[i] == null) {
                    itemStacks[i] = itemTags.getCraftItemstack(
                            Material.getMaterial(itemProperties[1]),
                            Integer.parseInt(itemProperties[0]),
                            Short.parseShort(itemProperties[2])
                    );
                    //noinspection deprecation
                    itemStacks[i].getData().setData(Byte.parseByte(itemProperties[3]));

                    if (itemProperties.length > 4)
                        itemUtil.safelyAddEnchantments(itemStacks[i], signshopUtil.convertStringToEnchantments(itemProperties[4]));
                }

                if (itemProperties.length > 5) {
                    try {
                        itemStacks[i] = SignShopBooks.addBooksProps(itemStacks[i], Integer.parseInt(itemProperties[5]));
                    } catch (NumberFormatException ignored) {

                    }
                }
                if (itemProperties.length > 6) {
                    try {
                        SignShopItemMeta.setMetaForID(itemStacks[i], Integer.parseInt(itemProperties[6]));
                    } catch (NumberFormatException ignored) {

                    }
                }
            } catch (Exception ignored) {

            }
        }

        if (invalidItems > 0) {
            ItemStack[] temp = new ItemStack[itemStringList.size() - invalidItems];
            int counter = 0;
            for (ItemStack i : itemStacks) {
                if (i != null) {
                    temp[counter] = i;
                    counter++;
                }
            }

            itemStacks = temp;
        }

        return itemStacks;
    }

    /**
     * Converts ItemStack array to old format strings.
     *
     * <p><b>Note:</b> This method is preserved for potential emergency rollback only.
     * It is NOT used in normal operation.</p>
     *
     * @param itemStackArray Array of ItemStacks to convert
     * @return Array of old format strings (2x length of input for redundancy)
     * @deprecated Only preserved for emergency rollback, not actively used
     */
    //Probably won't need this but saving it anyway.
    public static String[] convertItemStacksToOldString(ItemStack[] itemStackArray) {
        List<String> itemStringList = new ArrayList<>();
        if (itemStackArray == null)
            return new String[1];

        ItemStack currentItemStack;
        for (ItemStack itemStack : itemStackArray) {
            if (itemStack != null) {
                currentItemStack = itemStack;
                String ID = "";
                if (itemUtil.isWriteableBook(currentItemStack))
                    ID = SignShopBooks.getBookID(currentItemStack).toString();
                String metaID = SignShopItemMeta.getMetaID(currentItemStack).toString();
                if (metaID.equals("-1"))
                    metaID = "";
                ItemStack[] stacks = new ItemStack[1];
                stacks[0] = currentItemStack;

                itemStringList.add(BukkitSerialization.itemStackArrayToBase64(stacks));

                //noinspection deprecation
                itemStringList.add((currentItemStack.getAmount() + Storage.getItemSeperator()
                        + currentItemStack.getType() + Storage.getItemSeperator()
                        + ((Damageable) currentItemStack.getItemMeta()).getDamage() + Storage.getItemSeperator()
                        + currentItemStack.getData().getData() + Storage.getItemSeperator()
                        + signshopUtil.convertEnchantmentsToString(currentItemStack.getEnchantments()) + Storage.getItemSeperator()
                        + ID + Storage.getItemSeperator()
                        + metaID + Storage.getItemSeperator()
                        + "|" + BukkitSerialization.itemStackArrayToBase64(stacks)));
            }

        }
        String[] items = new String[itemStringList.size()];
        itemStringList.toArray(items);
        return items;
    }
}
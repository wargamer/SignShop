package org.wargamer2010.signshop.incompatibility.detectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.incompatibility.IncompatibilityDetector;
import org.wargamer2010.signshop.incompatibility.IncompatibilityType;

import java.util.NoSuchElementException;

/**
 * Detects custom player heads with empty name fields that are incompatible with Spigot 1.21.10+.
 *
 * <p><b>The Problem:</b></p>
 * <p>Old custom head plugins (HeadDatabase pre-v4.20.3, Head Database, custom resource packs)
 * created player heads with empty string names in the skull-owner field. Spigot 1.21.10+
 * added stricter validation that throws NPE when encountering these empty names:</p>
 *
 * <pre>
 * Custom head structure:
 *   skull-owner:
 *     uniqueId: 00000000-0000-0000-0000-000000000000
 *     name: ''  ← EMPTY STRING (not null)
 *
 * Spigot 1.21.10+ code:
 *   Optional.of(name)           // name is "", not null
 *     .filter(n -> !n.isEmpty()) // Filters out empty string
 *     .get()                     // NPE! Optional is now empty
 * </pre>
 *
 * <p><b>Detection Method:</b></p>
 * <ol>
 *   <li>Check if item is a PLAYER_HEAD</li>
 *   <li>Check if skull has an owning player</li>
 *   <li>Try to get the player name - if it's null/empty or throws NoSuchElementException, it's incompatible</li>
 * </ol>
 *
 * <p><b>Affected Plugins:</b></p>
 * <ul>
 *   <li>HeadDatabase (versions before 4.20.3)</li>
 *   <li>Head Database (different plugin)</li>
 *   <li>Custom resource pack heads</li>
 *   <li>Other custom head generators that left name field empty</li>
 * </ul>
 *
 * @author SignShop Development Team
 * @version 5.2.0
 * @since 5.2.0
 */
public class PlayerHeadIncompatibilityDetector implements IncompatibilityDetector {

    // ========================================
    // Version Constants
    // ========================================

    /** First Spigot version with stricter validation */
    private static final String FIRST_AFFECTED_VERSION = "1.21.10";

    /** Version where this was fixed (null = not fixed yet) */
    private static final String FIXED_IN_VERSION = null;

    // ========================================
    // Detection Logic
    // ========================================

    /**
     * Detects if a player head has an empty name field.
     *
     * <p><b>Detection Strategy:</b></p>
     * <ol>
     *   <li>Quick checks: null, not a player head, no meta → return null (compatible)</li>
     *   <li>Check if skull has owning player → no owner → return null (vanilla head)</li>
     *   <li>Try to get player name:
     *     <ul>
     *       <li>If null or empty → INCOMPATIBLE</li>
     *       <li>If throws NoSuchElementException → INCOMPATIBLE</li>
     *       <li>Otherwise → compatible</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * @param item The ItemStack to check
     * @return PLAYER_HEAD_EMPTY_NAME if incompatible, null if compatible
     */
    @Override
    public IncompatibilityType detect(ItemStack item) {
        // Quick rejection checks
        if (item == null || item.getType() != Material.PLAYER_HEAD) {
            return null; // Not a player head
        }

        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof SkullMeta)) {
            return null; // No skull meta (shouldn't happen, but be safe)
        }

        SkullMeta skullMeta = (SkullMeta) meta;

        // Check if head has an owning player
        if (skullMeta.getOwningPlayer() == null) {
            return null; // Vanilla player head without custom texture
        }

        // Try to get the player name - this is where the NPE occurs in Spigot 1.21.10+
        try {
            String ownerName = skullMeta.getOwningPlayer().getName();

            // Empty or null name = incompatible
            if (ownerName == null || ownerName.isEmpty()) {
                debugLog("Detected empty name in player head (UUID: " +
                        skullMeta.getOwningPlayer().getUniqueId() + ")");
                return IncompatibilityType.PLAYER_HEAD_EMPTY_NAME;
            }

        } catch (NoSuchElementException e) {
            // getName() threw exception - definitely incompatible
            debugLog("Detected player head with NoSuchElementException on getName()");
            return IncompatibilityType.PLAYER_HEAD_EMPTY_NAME;

        } catch (Exception e) {
            // Any other exception during name retrieval is suspicious
            debugLog("Unexpected exception checking player head: " + e.getMessage());
            return IncompatibilityType.PLAYER_HEAD_EMPTY_NAME;
        }

        // No incompatibility detected
        return null;
    }

    // ========================================
    // Version Checking
    // ========================================

    /**
     * Checks if this incompatibility affects the current Spigot version.
     *
     * <p><b>Logic:</b></p>
     * <ul>
     *   <li>Check if version contains "1.21.1" (covers 1.21.10 through 1.21.19)</li>
     *   <li>Or check if version contains "1.21.2" or higher</li>
     *   <li>Returns false if fixed version is known and current version is >= fixed version</li>
     * </ul>
     *
     * @return true if current Spigot version is affected by this incompatibility
     */
    @Override
    public boolean isRelevantForCurrentVersion() {
        String version = Bukkit.getVersion();

        // If there's a known fix version, check if we're past it
        if (FIXED_IN_VERSION != null && isVersionGreaterOrEqual(version)) {
            return false; // Issue is fixed in this version
        }

        // Check if we're on an affected version (1.21.10+)
        // This is a simplified check - in production you'd want proper version parsing
        return version.contains("1.21.1") ||  // 1.21.10-1.21.19
                version.contains("1.21.2") ||  // 1.21.20+
                version.contains("1.22") ||    // Future versions
                version.contains("1.23");      // Future versions
    }

    /**
     * Gets the version where this incompatibility was fixed.
     *
     * @return null (not fixed yet as of this implementation)
     */
    @Override
    public String getFixedInVersion() {
        return FIXED_IN_VERSION;
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Simplified version comparison.
     *
     * <p>NOTE: This is a basic implementation. For production use, consider
     * a more robust version comparison library.</p>
     *
     * @param current Current version string
     * @return true if current >= target
     */
    private boolean isVersionGreaterOrEqual(String current) {
        // Extract version numbers (simplified - doesn't handle all edge cases)
        try {
            String currentClean = current.replaceAll("[^0-9.]", "");
            String targetClean = PlayerHeadIncompatibilityDetector.FIXED_IN_VERSION.replaceAll("[^0-9.]", "");

            String[] currentParts = currentClean.split("\\.");
            String[] targetParts = targetClean.split("\\.");

            int maxLength = Math.max(currentParts.length, targetParts.length);

            for (int i = 0; i < maxLength; i++) {
                int currentPart = i < currentParts.length ?
                        Integer.parseInt(currentParts[i]) : 0;
                int targetPart = i < targetParts.length ?
                        Integer.parseInt(targetParts[i]) : 0;

                if (currentPart > targetPart) return true;
                if (currentPart < targetPart) return false;
            }

            return true; // Equal

        } catch (NumberFormatException e) {
            // If parsing fails, assume version is relevant (safer)
            return false;
        }
    }

    /**
     * Logs debug messages when debugging is enabled.
     *
     * @param message Debug message to log
     */
    private void debugLog(String message) {
        SignShop.getInstance().debugClassMessage(
                message,
                "PlayerHeadIncompatibilityDetector"
        );
    }
}
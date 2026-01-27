package org.wargamer2010.signshop.incompatibility;

import org.bukkit.inventory.ItemStack;

/**
 * Interface for detecting item incompatibilities in specific Spigot versions.
 *
 * <p>Implementations of this interface check for specific types of item incompatibilities
 * that may prevent items from being serialized or deserialized correctly.</p>
 *
 * <p><b>Design Pattern:</b> Strategy Pattern - Each detector encapsulates a specific
 * incompatibility check.</p>
 *
 * <p><b>Example Implementation:</b></p>
 * <pre>
 * public class PlayerHeadIncompatibilityDetector implements IncompatibilityDetector {
 *
 *     {@literal @}Override
 *     public IncompatibilityType detect(ItemStack item) {
 *         if (item == null || item.getType() != Material.PLAYER_HEAD) {
 *             return null; // Not a player head
 *         }
 *
 *         // Check for empty name...
 *         if (hasEmptyName(item)) {
 *             return IncompatibilityType.PLAYER_HEAD_EMPTY_NAME;
 *         }
 *
 *         return null; // No incompatibility
 *     }
 *
 *     {@literal @}Override
 *     public boolean isRelevantForCurrentVersion() {
 *         String version = Bukkit.getVersion();
 *         return version.contains("1.21.10") || version.contains("1.21.1");
 *     }
 *
 *     {@literal @}Override
 *     public String getFixedInVersion() {
 *         return null; // Not fixed yet
 *     }
 * }
 * </pre>
 *
 * @author SignShop Development Team
 * @version 5.2.0
 * @since 5.2.0
 */
public interface IncompatibilityDetector {

    /**
     * Detects if an item has an incompatibility.
     *
     * <p><b>Contract:</b></p>
     * <ul>
     *   <li>Return null if the item is compatible</li>
     *   <li>Return an IncompatibilityType if an issue is detected</li>
     *   <li>Should NOT throw exceptions - return null instead</li>
     *   <li>Should be fast - this may be called frequently</li>
     * </ul>
     *
     * @param item The ItemStack to check (maybe null)
     * @return IncompatibilityType if incompatible, null if compatible
     */
    IncompatibilityType detect(ItemStack item);

    /**
     * Checks if this detector is relevant for the current Spigot version.
     *
     * <p>This allows detectors to be automatically disabled if running on a Spigot
     * version where the incompatibility doesn't exist (e.g., if Spigot fixes the bug).</p>
     *
     * <p><b>Example:</b></p>
     * <pre>
     * public boolean isRelevantForCurrentVersion() {
     *     String version = Bukkit.getVersion();
     *     // Only relevant for Spigot 1.21.10 through 1.21.19
     *     return version.contains("1.21.1") && !version.contains("1.21.2");
     * }
     * </pre>
     *
     * @return true if this incompatibility affects the current Spigot version
     */
    boolean isRelevantForCurrentVersion();

    /**
     * Gets the Spigot version where this incompatibility was fixed (if known).
     *
     * <p>This is informational and helps track when issues are resolved.</p>
     *
     * <p><b>Returns:</b></p>
     * <ul>
     *   <li>Version string (e.g. "1.21.20") if the issue is fixed</li>
     *   <li>null if the issue is not yet fixed</li>
     * </ul>
     *
     * @return Version string where fixed, or null if not fixed yet
     */
    String getFixedInVersion();
}
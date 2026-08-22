package org.wargamer2010.signshop.incompatibility;

import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.incompatibility.detectors.PlayerHeadIncompatibilityDetector;

import java.util.ArrayList;
import java.util.List;

/**
 * Main API for checking item incompatibilities.
 *
 * <p>This utility class manages all incompatibility detectors and provides
 * convenient methods for checking single items or arrays of items.</p>
 *
 * <p><b>Usage Examples:</b></p>
 *
 * <pre>
 * // Check a single item
 * ItemStack item = ...;
 * IncompatibilityType issue = IncompatibilityChecker.checkItem(item);
 * if (issue != null) {
 *     System.out.println("Item is incompatible: " + issue.getDescription());
 * }
 *
 * // Check an array of items
 * ItemStack[] items = ...;
 * if (IncompatibilityChecker.hasIncompatibleItems(items)) {
 *     System.out.println("At least one item is incompatible");
 * }
 *
 * // Get all incompatibilities in an array
 * List&lt;IncompatibilityType&gt; issues = IncompatibilityChecker.checkAll(items);
 * for (IncompatibilityType issue : issues) {
 *     System.out.println("Found issue: " + issue);
 * }
 * </pre>
 *
 * <p><b>Extensibility:</b></p>
 * <p>To add a new detector, simply add it to the DETECTORS list. The framework
 * will automatically use it for all checks.</p>
 *
 * @author SignShop Development Team
 * @version 5.2.0
 * @since 5.2.0
 */
public class IncompatibilityChecker {

    // ========================================
    // Detector Registry
    // ========================================

    /**
     * List of all active incompatibility detectors.
     *
     * <p><b>To add a new detector:</b></p>
     * <ol>
     *   <li>Create a class implementing IncompatibilityDetector</li>
     *   <li>Add an instance to this list</li>
     *   <li>Done! The framework will automatically use it</li>
     * </ol>
     */
    private static final List<IncompatibilityDetector> DETECTORS = List.of(
            new PlayerHeadIncompatibilityDetector()
            // Add new detectors here:
            // new ShulkerBoxIncompatibilityDetector(),
            // new BundleIncompatibilityDetector(),
            // etc.
    );

    // ========================================
    // Private Constructor (Utility Class)
    // ========================================

    /**
     * Private constructor - this is a utility class with only static methods.
     */
    private IncompatibilityChecker() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ========================================
    // Single Item Checks
    // ========================================

    /**
     * Checks if a single item has any incompatibilities.
     *
     * <p>Runs all registered detectors that are relevant for the current Spigot version.
     * Returns the first incompatibility found, or null if the item is compatible.</p>
     *
     * <p><b>Performance:</b> Fast - detectors short-circuit on first match.</p>
     *
     * @param item The ItemStack to check (null is safe, returns null)
     * @return First IncompatibilityType found, or null if compatible
     */
    public static IncompatibilityType checkItem(ItemStack item) {
        if (item == null) {
            return null; // Null items are "compatible" (no issue)
        }

        // Run all relevant detectors
        for (IncompatibilityDetector detector : DETECTORS) {
            // Skip detectors not relevant for current version
            if (!detector.isRelevantForCurrentVersion()) {
                continue;
            }

            // Check for incompatibility
            IncompatibilityType issue = detector.detect(item);
            if (issue != null) {
                return issue; // Found an incompatibility
            }
        }

        // No incompatibilities found
        return null;
    }

    /**
     * Checks if a single item is compatible (has no incompatibilities).
     *
     * <p>Convenience method equivalent to: {@code checkItem(item) == null}</p>
     *
     * @param item The ItemStack to check
     * @return true if compatible, false if incompatible
     */
    public static boolean isCompatible(ItemStack item) {
        return checkItem(item) == null;
    }

    /**
     * Checks if a single item is incompatible (has at least one incompatibility).
     *
     * <p>Convenience method equivalent to: {@code checkItem(item) != null}</p>
     *
     * @param item The ItemStack to check
     * @return true if incompatible, false if compatible
     */
    public static boolean isIncompatible(ItemStack item) {
        return checkItem(item) != null;
    }

    // ========================================
    // Array Checks
    // ========================================

    /**
     * Checks if any items in an array have incompatibilities.
     *
     * <p>Returns true as soon as the first incompatible item is found (short-circuits).</p>
     *
     * @param items Array of ItemStacks to check (null-safe)
     * @return true if at least one item is incompatible
     */
    public static boolean hasIncompatibleItems(ItemStack[] items) {
        if (items == null) {
            return false;
        }

        for (ItemStack item : items) {
            if (isIncompatible(item)) {
                return true; // Found an incompatible item
            }
        }

        return false; // All items are compatible
    }

    /**
     * Checks all items in an array and returns all incompatibilities found.
     *
     * <p>Unlike hasIncompatibleItems(), this method checks ALL items and returns
     * a list of all incompatibility types found (may contain duplicates if multiple
     * items have the same issue).</p>
     *
     * <p><b>Use this when you need to:</b></p>
     * <ul>
     *   <li>Know about ALL incompatibilities, not just the first one</li>
     *   <li>Generate detailed reports or statistics</li>
     *   <li>Log all issues for debugging</li>
     * </ul>
     *
     * @param items Array of ItemStacks to check (null-safe)
     * @return List of all IncompatibilityTypes found (empty list if all compatible)
     */
    public static List<IncompatibilityType> checkAll(ItemStack[] items) {
        List<IncompatibilityType> issues = new ArrayList<>();

        if (items == null) {
            return issues; // Empty list
        }

        for (ItemStack item : items) {
            IncompatibilityType issue = checkItem(item);
            if (issue != null) {
                issues.add(issue);
            }
        }

        return issues;
    }

    /**
     * Counts how many items in an array are incompatible.
     *
     * @param items Array of ItemStacks to check (null-safe)
     * @return Number of incompatible items (0 if all compatible)
     */
    public static int countIncompatible(ItemStack[] items) {
        if (items == null || items.length == 0) {
            return 0;
        }

        int count = 0;
        for (ItemStack item : items) {
            if (isIncompatible(item)) {
                count++;
            }
        }

        return count;
    }

    // ========================================
    // Detector Management
    // ========================================

    /**
     * Gets all registered detectors.
     *
     * <p>Returns an unmodifiable view of the detector list.</p>
     *
     * @return List of all registered detectors
     */
    public static List<IncompatibilityDetector> getDetectors() {
        return new ArrayList<>(DETECTORS); // Defensive copy
    }

    /**
     * Gets all detectors that are relevant for the current Spigot version.
     *
     * <p>Useful for debugging or reporting which checks are active.</p>
     *
     * @return List of relevant detectors
     */
    public static List<IncompatibilityDetector> getRelevantDetectors() {
        List<IncompatibilityDetector> relevant = new ArrayList<>();

        for (IncompatibilityDetector detector : DETECTORS) {
            if (detector.isRelevantForCurrentVersion()) {
                relevant.add(detector);
            }
        }

        return relevant;
    }

    /**
     * Gets the number of registered detectors.
     *
     * @return Total number of detectors
     */
    public static int getDetectorCount() {
        return DETECTORS.size();
    }

    /**
     * Gets the number of detectors relevant for the current version.
     *
     * @return Number of active detectors
     */
    public static int getRelevantDetectorCount() {
        int count = 0;
        for (IncompatibilityDetector detector : DETECTORS) {
            if (detector.isRelevantForCurrentVersion()) {
                count++;
            }
        }
        return count;
    }
}
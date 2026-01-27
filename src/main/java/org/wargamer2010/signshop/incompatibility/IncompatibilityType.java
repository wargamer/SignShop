package org.wargamer2010.signshop.incompatibility;

/**
 * Defines types of item incompatibilities that can occur in different Spigot versions.
 *
 * <p>Each incompatibility type includes:
 * <ul>
 *   <li><b>Description:</b> Human-readable explanation of the issue</li>
 *   <li><b>Affected Versions:</b> Which Spigot versions have this problem</li>
 *   <li><b>Solution:</b> How users can fix the issue</li>
 * </ul>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>
 * IncompatibilityType issue = IncompatibilityType.PLAYER_HEAD_EMPTY_NAME;
 * System.out.println(issue.getDescription());
 * System.out.println("Affected: " + issue.getAffectedVersions());
 * System.out.println("Solution: " + issue.getSolution());
 * </pre>
 *
 * @author SignShop Development Team
 * @version 5.2.0
 * @since 5.2.0
 */
public enum IncompatibilityType {

    /**
     * Custom player heads with empty name fields from various custom head plugins.
     *
     * <p><b>Root Cause:</b> Old custom head plugins (HeadDatabase pre-v4.20.3, Head Database,
     * custom resource packs) created player heads with empty string names. Spigot 1.21.10+
     * added stricter validation that throws NPE when encountering empty names.</p>
     *
     * <p><b>Impact:</b> Shops with these heads cannot deserialize items, causing NPE during
     * YAML deserialization or legacy deserialization.</p>
     */
    PLAYER_HEAD_EMPTY_NAME(
            "Custom player head with empty name field",
            "Spigot 1.21.10+",
            "Remove old custom heads and recreate shop with new heads from updated plugins"
    ),

    /**
     * Placeholder for future shulker box incompatibilities.
     *
     * <p>Example: Shulker boxes with corrupted inventory data in certain versions.</p>
     */
    SHULKER_BOX_INVALID_DATA(
            "Shulker box with invalid inventory data",
            "Unknown (future issue)",
            "Transfer contents to new shulker box"
    ),

    /**
     * Unknown or unclassified incompatibility detected.
     *
     * <p>Used when an item fails validation but doesn't match any known patterns.</p>
     */
    UNKNOWN(
            "Unknown item incompatibility",
            "Unknown",
            "Contact server administrator or plugin support"
    );

    // ========================================
    // Fields
    // ========================================

    private final String description;
    private final String affectedVersions;
    private final String solution;

    // ========================================
    // Constructor
    // ========================================

    /**
     * Creates an incompatibility type definition.
     *
     * @param description Human-readable description of the issue
     * @param affectedVersions Which Spigot versions are affected (e.g. "1.21.10+")
     * @param solution How users can resolve the issue
     */
    IncompatibilityType(String description, String affectedVersions, String solution) {
        this.description = description;
        this.affectedVersions = affectedVersions;
        this.solution = solution;
    }

    // ========================================
    // Getters
    // ========================================

    /**
     * Gets the human-readable description of this incompatibility.
     *
     * @return Description string
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the Spigot versions affected by this incompatibility.
     *
     * @return Version string (e.g. "1.21.10+", "1.20.0-1.20.4")
     */
    public String getAffectedVersions() {
        return affectedVersions;
    }

    /**
     * Gets the recommended solution for this incompatibility.
     *
     * @return Solution string
     */
    public String getSolution() {
        return solution;
    }

    // ========================================
    // Utility Methods
    // ========================================

    /**
     * Gets a formatted string suitable for logging or error messages.
     *
     * @return Formatted string with all information
     */
    public String getFormattedMessage() {
        return String.format("[%s] %s (Affected: %s) - Solution: %s",
                this.name(),
                description,
                affectedVersions,
                solution
        );
    }

    @Override
    public String toString() {
        return getFormattedMessage();
    }
}
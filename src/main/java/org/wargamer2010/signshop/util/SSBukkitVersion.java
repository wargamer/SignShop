
package org.wargamer2010.signshop.util;

/**
 * Bukkit version categories for API compatibility checks.
 *
 * <p>Used to determine which Bukkit APIs are available and how to serialize/deserialize items.</p>
 *
 * <ul>
 *   <li><b>Pre145:</b> Before 1.4.5-R0.3 - No ItemMeta support, legacy serialization</li>
 *   <li><b>Post145:</b> 1.4.5-R0.3 and later - Full ItemMeta support, modern serialization</li>
 *   <li><b>Unknown:</b> Could not determine version</li>
 *   <li><b>TBD:</b> Not yet checked (initial state)</li>
 * </ul>
 *
 * @see versionUtil#getBukkitVersionType()
 */
public enum SSBukkitVersion {
    Pre145,
    Post145,
    Unknown,
    TBD,
}


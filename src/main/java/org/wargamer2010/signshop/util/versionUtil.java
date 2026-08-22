package org.wargamer2010.signshop.util;

import org.bukkit.Bukkit;
import org.wargamer2010.signshop.SignShop;

import java.util.logging.Level;

/**
 * Bukkit/Spigot version detection and compatibility checking.
 *
 * <p>Parses the Bukkit version string to determine API compatibility level.
 * Results are cached for performance since version doesn't change at runtime.</p>
 *
 * <h2>Version Categories:</h2>
 * <ul>
 *   <li><b>Pre145:</b> Bukkit 1.4.5-R0.2 and earlier (no ItemMeta support)</li>
 *   <li><b>Post145:</b> Bukkit 1.4.5-R0.3+ (modern API with ItemMeta)</li>
 * </ul>
 *
 * <h2>Two numbering schemes</h2>
 * <p>Minecraft numbered releases 1.0 up to 1.21.11, then switched to a year based
 * scheme starting with 26.1 (26.1, 26.1.1, 26.1.2, 26.2, ...). Both look the same to
 * the comparison below: the parts are compared as numbers, and since 26 &gt; 1 every
 * year based release sorts above every 1.x release.</p>
 *
 * @see SSBukkitVersion
 */
public class versionUtil {
    private static SSBukkitVersion cachedVersion = SSBukkitVersion.TBD;
    private static int[] cachedServerVersion = null;

    private versionUtil() {

    }

    /**
     * Attempts to read the current Bukkit version and returns the result as a SSBukkitVersion
     * @return the detected Bukkit version or Unknown
     */
    public static SSBukkitVersion getBukkitVersionType() {
        if(cachedVersion == SSBukkitVersion.TBD) {
            String bukkitversion = Bukkit.getServer().getBukkitVersion();
            String[] versionbits = bukkitversion.split("-");
            if(versionbits.length < 2) {
                cachedVersion = SSBukkitVersion.Unknown;
                SignShop.log("Could not determine Bukkit compatibility from this string: " + bukkitversion, Level.SEVERE);
                return cachedVersion;
            }

            int cmp = compare(versionbits[0], "1.4.5");

            if(cmp < 0) // < 1.4.5
                cachedVersion = SSBukkitVersion.Pre145;
            else if(cmp == 0) { // == 1.4.5
                if(compare(versionbits[1], "R0.3") < 0) { // < 1.4.5-R0.3
                    cachedVersion = SSBukkitVersion.Pre145; // It didn't have support for ItemMeta so pre-Major-Overhaul
                } else { // >= 1.4.5-R0.3
                    cachedVersion = SSBukkitVersion.Post145;
                }
            }
            else // > 1.4.5
                cachedVersion = SSBukkitVersion.Post145;
        }

        return cachedVersion;
    }

    /**
     * The Minecraft version this server runs, split into its numeric parts.
     * "26.1.2-R0.1-SNAPSHOT" gives {26, 1, 2}, "1.21.11-R0.1-SNAPSHOT" gives {1, 21, 11}.
     *
     * @return the version parts, or an empty array if the version string made no sense
     */
    public static int[] getServerVersion() {
        if(cachedServerVersion == null)
            cachedServerVersion = parse(Bukkit.getServer().getBukkitVersion());

        return cachedServerVersion.clone();
    }

    /**
     * Checks whether the running server is at least the given Minecraft version.
     * Works across both numbering schemes, so isAtLeast("1.21.10") is also true on 26.1 and up.
     *
     * @param version version to check against, e.g. "1.21.10" or "26.1"
     * @return true if the server is that version or newer, false if it is older or unreadable
     */
    public static boolean isAtLeast(String version) {
        int[] server = getServerVersion();
        if(server.length == 0)
            return false;

        return compare(server, parse(version)) >= 0;
    }

    /**
     * Compare v1 with v2 and returns the result as an int
     *
     * @param v1 Version string #1
     * @param v2 Version string #2
     * @return {@literal Returns < 0 if v1 < v2, > 0 if v1 > v2, 0 if v1 == v2}
     */
    public static int compare(String v1, String v2) {
        return compare(parse(v1), parse(v2));
    }

    private static int compare(int[] v1, int[] v2) {
        for(int i = 0; i < Math.max(v1.length, v2.length); i++) {
            int left = i < v1.length ? v1[i] : 0;
            int right = i < v2.length ? v2[i] : 0;

            if(left != right)
                return left < right ? -1 : 1;
        }

        return 0;
    }

    /**
     * Pulls the numbers out of a version string. Anything that isn't a number is dropped,
     * so "1.21.11-R0.1-SNAPSHOT" stops at the dash and "R0.3" reads as {0, 3}.
     */
    private static int[] parse(String version) {
        if(version == null || version.isEmpty())
            return new int[0];

        String[] parts = version.split("-")[0].split("\\.");
        int[] numbers = new int[parts.length];
        int found = 0;

        for(String part : parts) {
            String digits = part.replaceAll("[^0-9]", "");
            if(digits.isEmpty())
                break;

            try {
                numbers[found++] = Integer.parseInt(digits);
            } catch(NumberFormatException e) {
                break; // Absurdly long number, nothing sensible left to compare
            }
        }

        if(found == numbers.length)
            return numbers;

        int[] trimmed = new int[found];
        System.arraycopy(numbers, 0, trimmed, 0, found);
        return trimmed;
    }
}

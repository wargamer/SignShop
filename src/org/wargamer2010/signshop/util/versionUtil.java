
package org.wargamer2010.signshop.util;

import java.util.logging.Level;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.wargamer2010.signshop.SignShop;

public class versionUtil {
    private static SSBukkitVersion cachedVersion = SSBukkitVersion.TBD;

    private versionUtil() {

    }

    /**
     * Attempts to read the current Bukkit version and returns the result as a SSBukkitVersion
     * @return the detected Bukkit version or Unknown
     */
    public static SSBukkitVersion getBukkitVersionType() {
        if(cachedVersion == SSBukkitVersion.TBD) {
            String bukkitversion = Bukkit.getServer().getBukkitVersion();
            String[] versionbits = new String[1];
            if(bukkitversion.contains("-"))
                versionbits = bukkitversion.split("-");
            if(versionbits.length < 2)
                return SSBukkitVersion.Unknown;

            int cmp = versionUtil.compare(versionbits[0], "1.4.5");

            if(cmp < 0) // < 1.4.5
                cachedVersion = SSBukkitVersion.Pre145;
            else if(cmp == 0) { // == 1.4.5
                if(versionUtil.compare(versionbits[1], "R0.3") < 0) { // < 1.4.5-R0.3
                    cachedVersion = SSBukkitVersion.Pre145; // It didn't have support for ItemMeta so pre-Major-Overhaul
                } else { // >= 1.4.5-R0.3
                    cachedVersion = SSBukkitVersion.Post145;
                }
            }
            else // > 1.4.5
                cachedVersion = SSBukkitVersion.Post145;
        }

        if(cachedVersion == SSBukkitVersion.Unknown)
            SignShop.log("Could not determine Bukkit compatibility from this string: " + Bukkit.getServer().getBukkitVersion(), Level.SEVERE);

        return cachedVersion;
    }


    /**
     * Compare v1 with v2 and returns the result as an int
     *
     * @param v1 Version string #1
     * @param v2 Version string #2
     * @return {@literal Returns < 0 if v1 < v2, > 0 if v1 > v2, 0 if v1 == v2}
     */
    public static int compare(String v1, String v2) {
        String s1 = normalisedVersion(v1);
        String s2 = normalisedVersion(v2);
        return s1.compareTo(s2);
    }

    private static String normalisedVersion(String version) {
        return normalisedVersion(version, ".", 4);
    }

    private static String normalisedVersion(String version, String sep, int maxWidth) {
        String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
        StringBuilder sb = new StringBuilder((version.length() * 4)); // * 4 since we'll be adding chars
        for (String s : split) {
            sb.append(String.format("%" + maxWidth + 's', s));
        }
        return sb.toString();
    }
}

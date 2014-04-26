
package org.wargamer2010.signshop.configuration;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Color;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.wargamer2010.signshop.util.signshopUtil;

public class ColorUtil {
    private static Map<Integer, String> colorLookup = new HashMap<Integer, String>();

    private ColorUtil() {

    }

    public static void init() {
        // Stock Minecraft colors
        colorLookup.put(8339378 , "purple");
        colorLookup.put(11685080 , "magenta");
        colorLookup.put(8073150 , "purple");
        colorLookup.put(6724056 , "light blue");
        colorLookup.put(5013401 , "cyan");
        colorLookup.put(5000268 , "gray");
        colorLookup.put(10066329 , "light gray");
        colorLookup.put(15892389 , "pink");
        colorLookup.put(14188339 , "orange");
        colorLookup.put(8375321 , "lime");
        colorLookup.put(11743532 , "red");
        colorLookup.put(2437522 , "blue");
        colorLookup.put(15066419 , "yellow");
        colorLookup.put(10040115 , "red");
        colorLookup.put(1644825 , "black");
        colorLookup.put(6704179 , "brown");
        colorLookup.put(6717235 , "green");
        colorLookup.put(16777215 , "white");
        colorLookup.put(3361970 , "blue");
        colorLookup.put(1973019 , "black");
        colorLookup.put(14188952 , "pink");
        colorLookup.put(14602026, "yellow");
        colorLookup.put(10511680, "brown");

        // Load colors that will help guessing custom colornames
        FileConfiguration config = new YamlConfiguration();
        config = configUtil.loadYMLFromJar(config, "colors.yml");
        if(config == null)
            return;
        for(Map.Entry<String, String> entry : configUtil.fetchStringStringHashMap("colors", config).entrySet()) {
            try {
                int hex = Integer.parseInt(entry.getKey(), 16);
                if(!colorLookup.containsKey(hex))
                    colorLookup.put(hex, entry.getValue());
            } catch(NumberFormatException ex) {
                continue;
            }
        }
    }

    public static String getColorAsString(Color color) {
        int rgb = color.asRGB();
        if(colorLookup.containsKey(rgb)) {
            return signshopUtil.capFirstLetter(colorLookup.get(rgb));
        } else {
            double diff = -1;
            String last = "";
            for(int val : colorLookup.keySet()) {
                double currentdiff = getDifferenceBetweenColors(rgb, val);
                if(diff == -1 || currentdiff < diff) {
                    diff = currentdiff;
                    last = colorLookup.get(val);
                }
            }
            return signshopUtil.capFirstLetter(last);
        }
    }

    public static double getDifferenceBetweenColors(int colorone, int colortwo) {
        java.awt.Color a = new java.awt.Color(colorone);
        java.awt.Color b = new java.awt.Color(colortwo);
        int comboa = (a.getRed() + a.getGreen() + a.getBlue());
        int combob = (b.getRed() + b.getGreen() + b.getBlue());
        return Math.abs(comboa - combob);
    }
}

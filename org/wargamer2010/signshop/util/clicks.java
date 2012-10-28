package org.wargamer2010.signshop.util;

import java.util.Collections;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import java.util.Map;
import java.util.LinkedHashMap;

public class clicks {
    public static Map<Location, Player> mClicksPerLocation = new LinkedHashMap<Location, Player>();
    public static Map<String, Player> mClicksPerPlayername = new LinkedHashMap<String, Player>();

    public static void init() {
        mClicksPerLocation = new LinkedHashMap<Location, Player>();
        mClicksPerPlayername = new LinkedHashMap<String, Player>();
    }

    public static void removePlayerFromClickmap(Player player) {
        clicks.mClicksPerLocation.values().removeAll(Collections.singleton(player));
    }
}

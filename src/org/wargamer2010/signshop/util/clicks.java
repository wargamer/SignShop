package org.wargamer2010.signshop.util;

import java.util.Collections;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import java.util.Map;
import java.util.LinkedHashMap;
import org.bukkit.entity.Entity;
import org.wargamer2010.signshop.player.PlayerIdentifier;

public class clicks {
    public static Map<Location, Player> mClicksPerLocation = new LinkedHashMap<Location, Player>();
    public static Map<PlayerIdentifier, Player> mClicksPerPlayerId = new LinkedHashMap<PlayerIdentifier, Player>();
    public static Map<Entity, Player> mClicksPerEntity = new LinkedHashMap<Entity, Player>();

    private clicks() {

    }

    public static void removePlayerFromClickmap(Player player) {
        clicks.mClicksPerLocation.values().removeAll(Collections.singleton(player));
    }

    public static void removePlayerFromEntityMap(Player player) {
        clicks.mClicksPerEntity.values().removeAll(Collections.singleton(player));
    }
}

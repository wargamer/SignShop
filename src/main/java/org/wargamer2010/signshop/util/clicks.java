package org.wargamer2010.signshop.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.wargamer2010.signshop.player.PlayerIdentifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tracks blocks and entities selected by players for shop creation.
 *
 * <p>When players left-click blocks with the link material (redstone dust),
 * those blocks are registered here. When creating a shop, these registered
 * blocks become the shop's containables (chests) and activatables (levers).</p>
 *
 * @see signshopUtil#registerClickedMaterial
 */
public class clicks {
    public static Map<Location, Player> mClicksPerLocation = new LinkedHashMap<>();
    public static Map<PlayerIdentifier, Player> mClicksPerPlayerId = new LinkedHashMap<>();
    public static Map<Entity, Player> mClicksPerEntity = new LinkedHashMap<>();

    private clicks() {

    }

    public static void removePlayerFromClickmap(Player player) {
        clicks.mClicksPerLocation.values().removeAll(Collections.singleton(player));
    }

    public static void removePlayerFromEntityMap(Player player) {
        clicks.mClicksPerEntity.values().removeAll(Collections.singleton(player));
    }
}

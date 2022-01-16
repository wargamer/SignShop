package org.wargamer2010.signshop.player;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerCache {

    private static final Map<UUID, PlayerIdentifier> cachedIdentifiers = new HashMap<>();
    private static final Map<PlayerIdentifier, SignShopPlayer> cachedPlayers = new HashMap<>();

    public static SignShopPlayer getPlayer(Player player) {
        PlayerIdentifier playerIdentifier = cachedIdentifiers.computeIfAbsent(player.getUniqueId(), v -> new PlayerIdentifier(player));
        cachedPlayers.computeIfAbsent(playerIdentifier, v -> new SignShopPlayer(playerIdentifier));
        return cachedPlayers.get(cachedIdentifiers.get(player.getUniqueId()));
    }

    public static void removeFromCache(Player player) {
        cachedPlayers.remove(cachedIdentifiers.get(player.getUniqueId()));
        cachedIdentifiers.remove(player.getUniqueId());
    }

}

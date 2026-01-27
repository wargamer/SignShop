package org.wargamer2010.signshop.player;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Cache for {@link SignShopPlayer} and {@link PlayerIdentifier} instances.
 *
 * <p>Avoids repeated object creation by caching player wrappers. Entries are
 * automatically created on first access and removed when players disconnect.</p>
 */
public class PlayerCache {

    private static final Map<UUID, PlayerIdentifier> cachedIdentifiers = new HashMap<>();
    private static final Map<PlayerIdentifier, SignShopPlayer> cachedPlayers = new HashMap<>();

    public static SignShopPlayer getPlayer(Player player) {
        PlayerIdentifier playerIdentifier = cachedIdentifiers.computeIfAbsent(player.getUniqueId(), v -> new PlayerIdentifier(player));
        cachedPlayers.computeIfAbsent(playerIdentifier, v -> new SignShopPlayer(playerIdentifier));
        return cachedPlayers.get(cachedIdentifiers.get(player.getUniqueId()));
    }

    public static SignShopPlayer getPlayer(PlayerIdentifier playerIdentifier){
        cachedPlayers.computeIfAbsent(playerIdentifier, v -> new SignShopPlayer(playerIdentifier));
        return cachedPlayers.get(playerIdentifier);
    }

    public static void removeFromCache(Player player) {
        cachedPlayers.remove(cachedIdentifiers.get(player.getUniqueId()));
        cachedIdentifiers.remove(player.getUniqueId());
    }

}

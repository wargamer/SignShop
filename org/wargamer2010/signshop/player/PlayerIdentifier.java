
package org.wargamer2010.signshop.player;

import java.lang.reflect.Method;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerIdentifier {
    private static boolean didMethodLookup = false;
    private static boolean uuidSupport = false;
    private UUID id = null;
    private String name = null;

    public PlayerIdentifier(Player player) {
        if(player != null) {
            if(GetUUIDSupport())
                id = player.getUniqueId();
            name = player.getName();
        }
    }

    public PlayerIdentifier(UUID pId) {
        if(pId != null) {
            id = pId;
            name = getName();
        }
    }

    public PlayerIdentifier(String pName) {
        name = pName;
        if(GetUUIDSupport()) {
            OfflinePlayer offplayer = getOfflinePlayer();
            if(offplayer != null)
                id = offplayer.getUniqueId();
        }
    }

    public String getStringIdentifier() {
        if(GetUUIDSupport()) {
            return id.toString();
        } else {
            return name;
        }
    }

    @SuppressWarnings("deprecation") // Backwards compatibility
    public Player getPlayer() {
        if(GetUUIDSupport()) {
            return id == null ? null : Bukkit.getPlayer(id);
        } else {
            return Bukkit.getPlayer(name);
        }
    }

    @SuppressWarnings("deprecation") // Backwards compatibility
    public final OfflinePlayer getOfflinePlayer() {
        if(GetUUIDSupport()) {
            return id == null ? null : Bukkit.getOfflinePlayer(id);
        } else {
            return Bukkit.getOfflinePlayer(name);
        }
    }

    public final String getName() {
        OfflinePlayer offplayer = getOfflinePlayer();
        if(offplayer != null)
            return offplayer.getName();

        return name;
    }

    public static SignShopPlayer getPlayerFromString(String string) {
        if(string == null || string.isEmpty())
            return null;

        if(GetUUIDSupport()) {
            try {
                return new SignShopPlayer(new PlayerIdentifier(UUID.fromString(string)));
            } catch(IllegalArgumentException ex) {
                // Legacy mode, name will be converted to UUID on next Save
            }
        }

        return getByName(string);
    }

    /**
     * This method should not be used, lookup by name has been deprecated
     * But it can be used for transition purposes
     *
     * Primarily used to convert the names to UUID in the sellers.yml
     *
     * @param name Player name
     * @return SignShopPlayer instance
     */
    @SuppressWarnings("deprecation") // Backwards compatibility
    public static SignShopPlayer getByName(String name) {
        if(name == null || name.isEmpty())
            return null;

        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        PlayerIdentifier id = null;

        if(player != null && player.getFirstPlayed() != 0)
            id = new PlayerIdentifier(player.getUniqueId());

        return new SignShopPlayer(id);
    }

    public static synchronized boolean GetUUIDSupport() {
        if(didMethodLookup)
            return uuidSupport;
        for(Method method : OfflinePlayer.class.getMethods()) {
            if(method.getName().equalsIgnoreCase("getUniqueId"))
                uuidSupport = true;
        }
        didMethodLookup = true;
        return uuidSupport;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlayerIdentifier other = (PlayerIdentifier) obj;
        if(other.getOfflinePlayer() == null)
            return getOfflinePlayer() == null;
        if(getOfflinePlayer() == null)
            return false;
        if(GetUUIDSupport())
            return other.getOfflinePlayer().getUniqueId().equals(getOfflinePlayer().getUniqueId());
        return other.getOfflinePlayer().getName().equals(getOfflinePlayer().getName());
    }

    @Override
    public String toString() {
        if(GetUUIDSupport())
            return id == null ? name : id.toString();
        return name;
    }
}

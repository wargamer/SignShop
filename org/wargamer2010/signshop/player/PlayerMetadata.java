
package org.wargamer2010.signshop.player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.plugin.Plugin;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.blocks.SSDatabase;

public class PlayerMetadata {
    private static final String filename = "player.db";
    private SignShopPlayer ssPlayer = null;
    private Plugin plugin = null;

    public PlayerMetadata(SignShopPlayer pPlayer, Plugin pPlugin) {
        ssPlayer = pPlayer;
        plugin = pPlugin;
    }

    public static void init() {
        SSDatabase metadb = new SSDatabase(filename);
        try {
            if(!metadb.tableExists("PlayerMeta"))
                metadb.runStatement("CREATE TABLE PlayerMeta ( PlayerMetaID INTEGER, Playername TEXT NOT NULL, Plugin TEXT NOT NULL, Metakey TEXT NOT NULL, Metavalue TEXT NOT NULL, PRIMARY KEY(PlayerMetaID) )", null, false);
        } finally {
            metadb.close();
        }
    }

    public boolean hasMeta(String key) {
        return (getMetaValue(key) != null);
    }

    public String getMetaValue(String key) {
        SSDatabase metadb = new SSDatabase(filename);
        Map<Integer, Object> params = new LinkedHashMap<Integer, Object>();
        params.put(1, plugin.getName());
        params.put(2, ssPlayer.GetIdentifier().toString());
        params.put(3, key);

        try {
            ResultSet set = (ResultSet)metadb.runStatement("SELECT Metavalue FROM PlayerMeta WHERE Plugin = ? AND Playername = ? AND Metakey = ?", params, true);
            if(set == null)
                return null;
            if(set.next())
                return set.getString("Metavalue");
            else
                return null;
        } catch(SQLException ex) {
            return null;
        } finally {
            metadb.close();
        }
    }

    public boolean setMetavalue(String key, String value) {
        if(getMetaValue(key) != null) {
            return updateMeta(key, value);
        }
        SSDatabase metadb = new SSDatabase(filename);
        try {
            Map<Integer, Object> params = new LinkedHashMap<Integer, Object>();
            params.put(1, plugin.getName());
            params.put(2, ssPlayer.GetIdentifier().toString());
            params.put(3, key);
            params.put(4, value);
            return (metadb.runStatement("INSERT INTO PlayerMeta(Plugin, Playername, Metakey, Metavalue) VALUES (?, ?, ?, ?)", params, false) != null);
        } finally {
            metadb.close();
        }

    }

    public boolean updateMeta(String key, String value) {
        SSDatabase metadb = new SSDatabase(filename);
        try {
            Map<Integer, Object> params = new LinkedHashMap<Integer, Object>();
            params.put(1, value);
            params.put(2, plugin.getName());
            params.put(3, ssPlayer.GetIdentifier().toString());
            params.put(4, key);
            return (metadb.runStatement("UPDATE PlayerMeta SET Metavalue = ? WHERE Plugin = ? AND Playername = ? AND Metakey = ?", params, false) != null);
        } finally {
            metadb.close();
        }
    }

    public boolean removeMeta(String key) {
        SSDatabase metadb = new SSDatabase(filename);
        try {
            Map<Integer, Object> params = new LinkedHashMap<Integer, Object>();
            params.put(1, plugin.getName());
            params.put(2, ssPlayer.GetIdentifier().toString());
            params.put(3, key);
            return (metadb.runStatement("DELETE FROM PlayerMeta WHERE Plugin = ? AND Playername = ? AND Metakey = ?", params, false) != null);
        } finally {
            metadb.close();
        }
    }

    public boolean removeMetakeyLike(String key) {
        SSDatabase metadb = new SSDatabase(filename);
        try {
            Map<Integer, Object> params = new LinkedHashMap<Integer, Object>();
            params.put(1, plugin.getName());
            params.put(2, ssPlayer.GetIdentifier().toString());
            params.put(3, key);
            return (metadb.runStatement("DELETE FROM PlayerMeta WHERE Plugin = ? AND Playername = ? AND Metakey LIKE ?", params, false) != null);
        } finally {
            metadb.close();
        }
    }

    /**
     * Attempts to convert all player names to UUID where needed
     * Called a single time on plugin startup
     * @param pPlugin Plugin
     */
    @SuppressWarnings("UnusedAssignment") // Assignment is used in case of exception
    public static void convertToUuid(Plugin pPlugin) {
        if(!PlayerIdentifier.GetUUIDSupport())
            return; // Legacy mode
        SSDatabase metadb = new SSDatabase(filename);
        Map<Integer, Object> params = new LinkedHashMap<Integer, Object>();
        params.put(1, pPlugin.getName());
        ToConvert lastAttempt = null;

        try {
            ResultSet set = (ResultSet)metadb.runStatement("SELECT Playername, Metakey, Metavalue FROM PlayerMeta WHERE Plugin = ?", params, true);
            if(set == null)
                return;
            List<ToConvert> toConverts = new LinkedList<ToConvert>();
            while(set.next()) {
                String playername = set.getString("Playername");
                String metakey = set.getString("Metakey");
                String metavalue = set.getString("Metavalue");
                if(playername == null || metakey == null)
                    continue;
                SignShopPlayer player = PlayerIdentifier.getPlayerFromString(playername);
                if(player == null)
                    continue;
                String id = player.GetIdentifier().toString();
                if(!playername.equalsIgnoreCase(id))
                    toConverts.add(new ToConvert(playername, id, metakey, metavalue));
            }
            set.close();

            if(toConverts.size() > 0) {
                SignShop.log("Starting conversion from Player name to UUID for PlayerMeta table. Please be patient and don't interrupt the process.", Level.INFO);
            }

            for(ToConvert convert : toConverts) {
                lastAttempt = convert;

                params.clear();
                params.put(1, pPlugin.getName());
                params.put(2, convert.playerName);
                params.put(3, convert.metakey);
                metadb.runStatement("DELETE FROM PlayerMeta WHERE Plugin = ? AND Playername = ? AND Metakey = ?", params, false);

                params.clear();
                params.put(1, pPlugin.getName());
                params.put(2, convert.newId);
                params.put(3, convert.metakey);
                params.put(4, convert.metavalue);
                metadb.runStatement("INSERT INTO PlayerMeta(Plugin, Playername, Metakey, Metavalue) VALUES (?, ?, ?, ?)", params, false);
            }
        } catch(SQLException ex) {
            SignShop.log("Failed to convert Player names to UUID in PlayerMeta table because: " + ex.getMessage(), Level.WARNING);
            if(lastAttempt != null)
                SignShop.log(String.format("Failed conversion at meta for player '%s' with metakey '%s'", lastAttempt.playerName, lastAttempt.metakey), Level.WARNING);
        } finally {
            metadb.close();
        }
    }

    private static class ToConvert {
        public String playerName;
        public String newId;
        public String metakey;
        public String metavalue;

        private ToConvert(String pPlayername, String pNewId, String pMetakey, String pMetavalue) {
            playerName = pPlayername;
            newId = pNewId;
            metakey = pMetakey;
            metavalue = pMetavalue;
        }
    }
}

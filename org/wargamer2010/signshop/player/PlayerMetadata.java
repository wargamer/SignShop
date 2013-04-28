
package org.wargamer2010.signshop.player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.plugin.Plugin;
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
        params.put(2, ssPlayer.getName());
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
        SSDatabase metadb = new SSDatabase(filename);
        try {
            Map<Integer, Object> params = new LinkedHashMap<Integer, Object>();
            params.put(1, plugin.getName());
            params.put(2, ssPlayer.getName());
            params.put(3, key);
            params.put(4, value);
            return (metadb.runStatement("INSERT INTO PlayerMeta(Plugin, Playername, Metakey, Metavalue) VALUES (?, ?, ?, ?)", params, false) != null);
        } finally {
            metadb.close();
        }

    }

    public boolean removeMeta(String key) {
        SSDatabase metadb = new SSDatabase(filename);
        try {
            Map<Integer, Object> params = new LinkedHashMap<Integer, Object>();
            params.put(1, plugin.getName());
            params.put(2, ssPlayer.getName());
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
            params.put(2, ssPlayer.getName());
            params.put(3, key);
            return (metadb.runStatement("DELETE FROM PlayerMeta WHERE Plugin = ? AND Playername = ? AND Metakey LIKE ?", params, false) != null);
        } finally {
            metadb.close();
        }
    }

}

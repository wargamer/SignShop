package org.wargamer2010.signshop.hooks;

import org.bukkit.plugin.Plugin;
import java.util.HashMap;
import org.bukkit.Bukkit;

public class HookManager {
    private static HashMap<String, Plugin> hooks = new HashMap<String, Plugin>();
    
    public static Boolean addHook(String sName) {
        Plugin pHook = Bukkit.getServer().getPluginManager().getPlugin(sName);
        if(pHook != null) {
            hooks.put(sName, pHook);
            return true;
        } else
            return false;
    }
    
    public static Plugin getHook(String sName) {
        if(hooks.containsKey(sName))
            return hooks.get(sName);
        else
            return null;
    }
}

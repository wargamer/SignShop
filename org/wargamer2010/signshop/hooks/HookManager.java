package org.wargamer2010.signshop.hooks;

import org.bukkit.plugin.Plugin;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import java.util.HashMap;
import java.util.Map;

public class HookManager {
    private static HashMap<String, Plugin> hooks = new HashMap<String, Plugin>();

    private HookManager() {

    }

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

    public static Boolean canBuild(Player player, Block block) {
        Boolean canBuild = true;
        for(Map.Entry<String, Plugin> hookEntry : hooks.entrySet()) {
            String hookClassname = (hookEntry.getKey() + "Hook");
            try {
                Class<Object> fc = (Class<Object>)Class.forName("org.wargamer2010.signshop.hooks."+hookClassname);
                Hook testHook = ((Hook)fc.newInstance());
                canBuild = testHook.canBuild(player, block);
                if(!canBuild)
                    break;
            } catch(ClassNotFoundException notfoundex) { }
            catch(InstantiationException instex) { }
            catch(IllegalAccessException illex) { }
        }
        return canBuild;

    }
}

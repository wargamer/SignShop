package org.wargamer2010.signshop.hooks;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class HookManager {
    private static final HashMap<Hook, Plugin> hooks = new HashMap<>();

    private HookManager() {

    }

    public static void addHook(String sName) {
        Plugin pHook = Bukkit.getServer().getPluginManager().getPlugin(sName);
        if(pHook != null) {
            Hook hook = createHook(sName);
            if(hook != null)
                hooks.put(hook, pHook);
        }
    }

    private static Hook createHook(String hookName) {
        String hookClassname = (hookName + "Hook");
        try {
            Class<?> fc = Class.forName("org.wargamer2010.signshop.hooks."+hookClassname);
            return ((Hook)fc.newInstance());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ignored) {
        }

        return null;
    }

    public static Plugin getHook(String sName) {
        for(Map.Entry<Hook, Plugin> entry : hooks.entrySet()) {
            if(entry.getKey().getName().equals(sName))
                return entry.getValue();
        }

        return null;
    }

    public static Boolean canBuild(Player player, Block block) {
        for(Hook hookEntry : hooks.keySet()) {
            if(!hookEntry.canBuild(player, block))
                return false;
        }
        return true;
    }

    public static boolean protectBlock(Player player, Block block) {
        for(Hook hookEntry : hooks.keySet()) {
            if(hookEntry.protectBlock(player, block))
                return true;
        }

        return false;
    }
}

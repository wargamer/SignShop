package org.wargamer2010.signshop.hooks;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.wargamer2010.signshop.SignShop;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages protection plugin integrations (hooks) for SignShop.
 *
 * <p>Automatically detects and registers hooks for protection plugins like
 * WorldGuard, Towny, GriefPrevention, Residence, Lands, LWC, and BlockLocker.
 * Provides centralized permission checking across all registered hooks.</p>
 *
 * @see Hook
 */
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
            Class<?> fc = Class.forName("org.wargamer2010.signshop.hooks." + hookClassname);

            return ((Hook) fc.getDeclaredConstructor().newInstance());

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            SignShop.getInstance().debugMessage(e.getMessage());
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

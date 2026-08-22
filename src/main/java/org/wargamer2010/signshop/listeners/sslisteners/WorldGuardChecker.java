package org.wargamer2010.signshop.listeners.sslisteners;


import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.hooks.HookManager;

import java.util.logging.Level;

/**
 * Internal listener that enforces WorldGuard's allow-shop flag requirement for shop creation unless bypassed.
 */
public class WorldGuardChecker implements Listener {
    private static StateFlag ALLOW_SHOP_FLAG;

    public static void registerWorldGuardFlag(Server server) {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("allow-shop", false);
            registry.register(flag);
            ALLOW_SHOP_FLAG = flag;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("allow-shop");
            if (existing instanceof StateFlag) {
                ALLOW_SHOP_FLAG = (StateFlag) existing;
            }
            else {
                SignShop.log("Conflicting 'allow-shop' flag with another plugin!", Level.WARNING);
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSBuildEvent(SSCreatedEvent event) {
        if (event.isCancelled() || !event.canBeCancelled() || event.getPlayer().getPlayer() == null)
            return;
        if (HookManager.getHook("WorldGuard") == null)
            return;

        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(event.getPlayer().getPlayer());
        Location loc = BukkitAdapter.adapt(event.getSign().getLocation());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        if (query.testState(loc, localPlayer, ALLOW_SHOP_FLAG)) {
            return;
        }

        if (event.getPlayer().isOp()) {
            event.getPlayer().sendMessage(SignShop.getInstance().getSignShopConfig().getError("region_allow_shops_but_op", event.getMessageParts()));
        }
        else if (event.getPlayer().hasBypassShopPlots("WorldGuard")) {
            event.getPlayer().sendMessage(SignShop.getInstance().getSignShopConfig().getError("region_allow_shops_but_perm", event.getMessageParts()));
        }
        else {
            event.getPlayer().sendMessage(SignShop.getInstance().getSignShopConfig().getError("region_does_not_allow_shops", event.getMessageParts()));
            event.setCancelled(true);
        }
    }
}

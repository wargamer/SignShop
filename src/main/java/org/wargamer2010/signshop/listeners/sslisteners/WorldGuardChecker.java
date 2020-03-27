package org.wargamer2010.signshop.listeners.sslisteners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.hooks.HookManager;

import java.util.Map;
import java.util.logging.Level;

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

        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        World world = BukkitAdapter.adapt(event.getPlayer().getWorld());
        RegionManager regions = regionContainer.get(world);
        Location loc = event.getSign().getLocation();
        BlockVector3 blockVector3 = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        for (ProtectedRegion r : regions.getApplicableRegions(blockVector3)) {
            for (Map.Entry<Flag<?>, Object> flag : r.getFlags().entrySet()) {
                if (flag.getKey().getName().equals("allow-shop")) {
                    if (flag.getKey() instanceof StateFlag) {
                        if (flag.getValue() == StateFlag.State.ALLOW) // allow-shop is true
                            return;
                    }
                }
            }
        }

        if (event.getPlayer().isOp()) {
            event.getPlayer().sendMessage(SignShopConfig.getError("region_allow_shops_but_op", event.getMessageParts()));
        }
        else if (event.getPlayer().hasBypassShopPlots("WorldGuard")) {
            event.getPlayer().sendMessage(SignShopConfig.getError("region_allow_shops_but_perm", event.getMessageParts()));
        }
        else {
            event.getPlayer().sendMessage(SignShopConfig.getError("region_does_not_allow_shops", event.getMessageParts()));
            event.setCancelled(true);
        }
    }
}

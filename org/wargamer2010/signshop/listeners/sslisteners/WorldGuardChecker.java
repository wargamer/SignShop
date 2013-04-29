
package org.wargamer2010.signshop.listeners.sslisteners;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Map;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.events.SSPostTransactionEvent;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.hooks.HookManager;

public class WorldGuardChecker implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSBuildEvent(SSCreatedEvent event) {
        if(event.isCancelled() || !event.canBeCancelled())
            return;
        if(HookManager.getHook("WorldGuard") == null)
            return;
        WorldGuardPlugin WG = (WorldGuardPlugin)HookManager.getHook("WorldGuard");

        World world = event.getPlayer().getWorld();
        for(ProtectedRegion r : WG.getRegionManager(world).getApplicableRegions(event.getSign().getLocation())) {
            for(Map.Entry<Flag<?>, Object> flag : r.getFlags().entrySet()) {
                if(flag.getKey().getName().equals("allow-shop")) {
                    if(flag.getKey() instanceof StateFlag) {
                        if(flag.getValue() == StateFlag.State.DENY) { // allow-shop is false
                            event.getPlayer().sendMessage(SignShopConfig.getError("region_does_not_allow_shops", event.getMessageParts()));
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }
}

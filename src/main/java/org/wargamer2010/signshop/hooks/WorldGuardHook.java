package org.wargamer2010.signshop.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * WorldGuard integration hook that checks build permissions using WorldGuard's region protection system.
 */
public class WorldGuardHook implements Hook  {

    @Override
    public String getName() {
        return "WorldGuard";
    }

    @Override
    public Boolean canBuild(Player player, Block block) {
        if(HookManager.getHook("WorldGuard") == null)
            return true;
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        Location loc = BukkitAdapter.adapt(block.getLocation());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        boolean canBypass = WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld());
        boolean canBuild = query.testState(loc, localPlayer, Flags.BUILD);
        return canBypass || canBuild;
    }

    @Override
    public Boolean protectBlock(Player player, Block block) {
        return false;
    }
}

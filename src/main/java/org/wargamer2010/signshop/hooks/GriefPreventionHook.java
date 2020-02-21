
package org.wargamer2010.signshop.hooks;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class GriefPreventionHook implements Hook {

    @Override
    public String getName() {
        return "GriefPrevention";
    }

    @Override
    public Boolean canBuild(Player player, Block block) {
        if(HookManager.getHook("GriefPrevention") == null)
            return true;
        GriefPrevention plugin = (GriefPrevention)HookManager.getHook("GriefPrevention");
        return (plugin.allowBuild(player, block.getLocation(), block.getType()) == null);
    }

    @Override
    public Boolean protectBlock(Player player, Block block) {
        return false;
    }
}

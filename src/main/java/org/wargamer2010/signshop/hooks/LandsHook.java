package org.wargamer2010.signshop.hooks;

import me.angeschossen.lands.api.integration.LandsIntegration;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.wargamer2010.signshop.SignShop;

/**
 * Lands integration hook that checks trust permissions in claimed land areas.
 */
public class LandsHook implements Hook {
    private final LandsIntegration landsIntegration = new LandsIntegration(SignShop.getInstance());
    @Override
    public String getName() {
        return "Lands";
    }

    @Override
    public Boolean canBuild(Player player, Block block) {
        if (HookManager.getHook("Lands") == null)
            return true;

        if (landsIntegration.isClaimed(block.getLocation())){
           return landsIntegration.getAreaByLoc(block.getLocation()).isTrusted(player.getUniqueId());

        }
        return true;
    }

    @Override
    public Boolean protectBlock(Player player, Block block) {
        return false;
    }
}

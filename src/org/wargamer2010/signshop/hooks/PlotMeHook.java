package org.wargamer2010.signshop.hooks;

import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class PlotMeHook implements Hook {

    @Override
    public String getName() {
        return "PlotMe";
    }

    @Override
    public Boolean canBuild(Player player, Block block) {
        if(HookManager.getHook("PlotMe") == null)
            return true;
        boolean foundAllowed = false;
        if(PlotManager.getPlots(block) == null)
            return true;
        for(Plot plot : PlotManager.getPlots(block).values()) {
            if(PlayerIdentifier.GetUUIDSupport()) {
                if(plot.isAllowed(player.getPlayer().getUniqueId())
                        || plot.getOwnerId().equals(player.getPlayer().getUniqueId())) {
                    foundAllowed = true;
                }
            } else {
                if(plot.isAllowed(player.getName()) || plot.getOwner().equals(player.getName()))
                    foundAllowed = true;
            }
        }
        return foundAllowed;
    }

    @Override
    public Boolean protectBlock(Player player, Block block) {
        return false;
    }
}

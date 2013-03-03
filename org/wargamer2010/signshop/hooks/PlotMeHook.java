package org.wargamer2010.signshop.hooks;

import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlotMeHook implements Hook {
    @Override
    public Boolean canBuild(Player player, Block block) {
        if(HookManager.getHook("PlotMe") == null)
            return true;
        boolean foundAllowed = false;
        if(PlotManager.getPlots(block) == null)
            return true;
        for(Plot plot : PlotManager.getPlots(block).values()) {            
            if(plot.isAllowed(player.getName()) || plot.getOwner().equals(player.getName()))
                foundAllowed = true;
        }
        return foundAllowed;
    }
}

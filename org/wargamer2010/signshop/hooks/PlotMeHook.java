package org.wargamer2010.signshop.hooks;

import com.worldcretornica.plotme_core.Plot;
import com.worldcretornica.plotme_core.PlotMapInfo;
import com.worldcretornica.plotme_core.PlotMeCoreManager;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;
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
        if(!PlayerIdentifier.GetUUIDSupport()) {
            SignShop.log("PlotMe dropped support for playername lookups, please update your server software to support UUID.", Level.WARNING);
            return true;
        }

        PlotMapInfo mapInfo = PlotMeCoreManager.getInstance().getMap(block.getWorld().getName());
        if(mapInfo == null)
            return true;

        boolean foundAllowed = false;

        for(Map.Entry<String, PlotMapInfo> info : PlotMeCoreManager.getInstance().getPlotMaps().entrySet()) {
            Plot plot = info.getValue().getPlot(info.getKey());
            if(plot != null) {
                if(plot.isAllowed(player.getPlayer().getUniqueId())
                        || plot.getOwnerId().equals(player.getPlayer().getUniqueId())) {
                    foundAllowed = true;
                }
            }
        }

        SignShopPlayer ssPlayer = new SignShopPlayer(player);
        if(!foundAllowed) {
            if(ssPlayer.isOp()) {
                ssPlayer.sendMessage(SignShopConfig.getError("not_allowed_on_plot_but_op", null));
                return true;
            } else {
                ssPlayer.sendMessage(SignShopConfig.getError("not_allowed_on_plot", null));
            }
        }

        return foundAllowed;
    }

    @Override
    public Boolean protectBlock(Player player, Block block) {
        return false;
    }
}

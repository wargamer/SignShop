package org.wargamer2010.signshop.hooks;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.api.IApi;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PreciousStonesHook implements Hook {

    @Override
    public String getName() {
        return "PreciousStones";
    }

    @Override
    public Boolean canBuild(Player player, Block block) {
        if(HookManager.getHook("PreciousStones") == null)
            return true;
        IApi api = PreciousStones.API();
        return (api.canPlace(player, block.getLocation()));
    }

    @Override
    public Boolean protectBlock(Player player, Block block) {
        return false;
    }
}

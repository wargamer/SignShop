package org.wargamer2010.signshop.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class TownyHook implements Hook {

    @Override
    public String getName() {
        return "Towny";
    }

    @Override
    public Boolean canBuild(Player player, Block block) {
        return true;
    }

    @Override
    public Boolean protectBlock(Player player, Block block) {
        return false;
    }
}

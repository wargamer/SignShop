package org.wargamer2010.signshop.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface Hook {
    public abstract String getName();

    public abstract Boolean canBuild(Player player, Block block);

    public abstract Boolean protectBlock(Player player, Block block);
}

package org.wargamer2010.signshop.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface Hook {
    String getName();

    Boolean canBuild(Player player, Block block);

    Boolean protectBlock(Player player, Block block);
}

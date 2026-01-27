package org.wargamer2010.signshop.specialops;

import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

/**
 * Interface for special operations that execute when players interact with shop signs.
 * Special operations handle shop management tasks like copying signs, changing owners, and linking blocks.
 */
public interface SignShopSpecialOp {
    Boolean runOperation(List<Block> clickedBlocks, PlayerInteractEvent event, Boolean ranSomething);
}

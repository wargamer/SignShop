package org.wargamer2010.signshop.specialops;

import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.List;

public interface SignShopSpecialOp {
    public Boolean runOperation(List<Block> clickedBlocks, PlayerInteractEvent event, Boolean ranSomething);
}

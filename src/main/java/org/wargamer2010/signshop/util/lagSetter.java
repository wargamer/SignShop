package org.wargamer2010.signshop.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Switch;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.data.Storage;

/**
 * Delayed task that turns off levers after a configurable delay.
 * Used by setRedStoneOnTemp operation for timed redstone activation.
 */
public class lagSetter implements Runnable{
    private final Block blockToChange;

    public lagSetter(Block blockToChange){
        this.blockToChange = blockToChange.getWorld().getBlockAt(blockToChange.getX(), blockToChange.getY(), blockToChange.getZ());
    }

    @Override
    public void run(){
        if(blockToChange.getType() == Material.getMaterial("LEVER") && blockToChange.getBlockData() instanceof Switch) {
            // Best effort, load 2 chunks around the block in the hope it's enough
            itemUtil.loadChunkByBlock(blockToChange, SignShop.getInstance().getSignShopConfig().getChunkLoadRadius());
            Switch switchLever = (Switch) blockToChange.getBlockData();
            switchLever.setPowered(false);
            blockToChange.setBlockData(switchLever);
            for(Seller seller : Storage.get().getShopsByBlock(blockToChange))
                seller.reloadBlocks();
        }
    }
}

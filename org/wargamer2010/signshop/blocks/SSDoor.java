package org.wargamer2010.signshop.blocks;

import org.bukkit.block.Block;
import org.bukkit.material.Door;

/**
 * Class that will temporarily fix the issues that exist with the Door class
 * Deprecation is accepted for this class since it is itself deprecated
 */
@SuppressWarnings("deprecation")
public class SSDoor extends Door {
    private Block bBottomHalf = null;

    public SSDoor(Block block) {
        super(block.getType(), block.getData());
        if(!isBottomHalf())
            bBottomHalf = block.getWorld().getBlockAt(block.getX(), block.getY()-1, block.getZ());
    }

    /**
     * @return whether this is the bottom half of the door
     */
    public final boolean isBottomHalf() {
        return ((super.getData() & 0x8) == 0x0);
    }

    /**
     * Get the data from the bottom half of the door
     *
     * @return data from the bottom half
     */
    @Override
    public byte getData() {
        if(bBottomHalf == null)
            return super.getData();
        else
            return bBottomHalf.getData();
    }

    /**
     * Set the data of the bottom half of the door
     * @param data data to be set
     */
    @Override
    public void setData(byte data) {
        if(bBottomHalf == null)
            super.setData(data);
        else
            bBottomHalf.setData(data);
    }
}

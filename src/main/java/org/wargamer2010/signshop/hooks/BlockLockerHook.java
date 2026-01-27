package org.wargamer2010.signshop.hooks;

import nl.rutgerkok.blocklocker.BlockLockerAPIv2;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * BlockLocker integration hook that checks access permissions for locked blocks.
 */
public class BlockLockerHook implements Hook {
    @Override
    public String getName() {
        return "BlockLocker";
    }

    @Override
    public Boolean canBuild(Player player, Block block) {
        if (HookManager.getHook("BlockLocker") == null)
            return true;
        return BlockLockerAPIv2.isAllowed(player,block,true);
    }

    @Override
    public Boolean protectBlock(Player player, Block block) {
        return false;
    }
}

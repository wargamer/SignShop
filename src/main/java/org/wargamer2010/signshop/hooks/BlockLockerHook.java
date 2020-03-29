package org.wargamer2010.signshop.hooks;

import nl.rutgerkok.blocklocker.BlockLockerAPI;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BlockLockerHook implements Hook {
    @Override
    public String getName() {
        return "BlockLocker";
    }

    @Override
    public Boolean canBuild(Player player, Block block) {
        if (HookManager.getHook("BlockLocker") == null)
            return true;
        return BlockLockerAPI.isAllowed(player,block,true);
    }

    @Override
    public Boolean protectBlock(Player player, Block block) {
        return false;
    }
}

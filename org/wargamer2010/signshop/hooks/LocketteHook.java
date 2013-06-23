package org.wargamer2010.signshop.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.yi.acru.bukkit.Lockette.Lockette;

public class LocketteHook implements Hook {

    @Override
    public String getName() {
        return "Lockette";
    }

    @Override
    public Boolean canBuild(Player player, Block block) {
        if(HookManager.getHook("Lockette") == null)
            return true;
        return (Lockette.isUser(block, player.getName(), false) || Lockette.isEveryone(block));
    }

    @Override
    public Boolean protectBlock(Player player, Block block) {
        return false;
    }
}

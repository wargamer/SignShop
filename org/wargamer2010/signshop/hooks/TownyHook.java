package org.wargamer2010.signshop.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import com.daemitus.deadbolt.Deadbolt;

public class TownyHook implements Hook {

    @Override
    public String getName() {
        return "Towny";
    }

    @Override
    public Boolean canBuild(Player player, Block block) {
        return true;
    }
}

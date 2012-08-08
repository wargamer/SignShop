package org.wargamer2010.signshop.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import com.daemitus.deadbolt.Deadbolt;

public class DeadboltHook {
    public static Boolean canBuild(Player player, Block block) {
        if(HookManager.getHook("Deadbolt") == null)
            return true;        
        return Deadbolt.isAuthorized(player, block);
    }
}

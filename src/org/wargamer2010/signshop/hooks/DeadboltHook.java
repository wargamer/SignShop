package org.wargamer2010.signshop.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import com.daemitus.deadbolt.Deadbolt;

public class DeadboltHook implements Hook {

    @Override
    public String getName() {
        return "Deadbolt";
    }

    @Override
    public Boolean canBuild(Player player, Block block) {
        if(HookManager.getHook("Deadbolt") == null)
            return true;
        try {
            return (!Deadbolt.isProtected(block) || Deadbolt.isAuthorized(player, block));
        } catch(NullPointerException ex) {
            // Deadbolt does not support checking for specific materials
            return true;
        }
    }

    @Override
    public Boolean protectBlock(Player player, Block block) {
        return false;
    }
}

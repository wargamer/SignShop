package org.wargamer2010.signshop.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.lwc.LWC;

public class LWCHook implements Hook {
    @Override
    public Boolean canBuild(Player player, Block block) {
        if(HookManager.getHook("LWC") == null)
            return true;
        
        LWC lwc = (((LWCPlugin) HookManager.getHook("LWC"))).getLWC();
        if(lwc != null)
            if(lwc.findProtection(block) != null)
                return lwc.canAccessProtection(player, block);
        
        return true;
    }
}

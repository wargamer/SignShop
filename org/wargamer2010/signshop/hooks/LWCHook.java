package org.wargamer2010.signshop.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;

public class LWCHook implements Hook {

    @Override
    public String getName() {
        return "LWC";
    }

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

    @Override
    public Boolean protectBlock(Player player, Block block) {
        if(HookManager.getHook("LWC") == null)
            return false;

        LWC lwc = (((LWCPlugin) HookManager.getHook("LWC"))).getLWC();
        if(lwc == null)
            return false;
        if(lwc.findProtection(block) == null) {
            Protection prot = lwc.getPhysicalDatabase().registerProtection(block.getTypeId(), Protection.Type.PRIVATE,
                    block.getWorld().getName(), player.getUniqueId().toString(), "", block.getX(), block.getY(), block.getZ());
            lwc.getPhysicalDatabase().saveProtection(prot);
            return true;
        }

        return false;
    }
}

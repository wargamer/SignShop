package org.wargamer2010.signshop.blocks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.wargamer2010.signshop.player.SignShopPlayer;

import org.wargamer2010.signshop.hooks.*;

public class SignShopChest {
    Block ssChest = null;

    public SignShopChest(Block bChest) {
        if(bChest.getType() == Material.getMaterial("CHEST"))
            ssChest = bChest;
    }
    
    public Boolean allowedToLink(SignShopPlayer ssPlayer) {
        if(ssChest == null)
            return true;        
        return HookManager.canBuild(ssPlayer.getPlayer(), ssChest);
    }
}

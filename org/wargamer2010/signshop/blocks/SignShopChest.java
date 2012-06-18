package org.wargamer2010.signshop.blocks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.wargamer2010.signshop.player.SignShopPlayer;

import org.wargamer2010.signshop.hooks.LWCHook;
import org.wargamer2010.signshop.hooks.LocketteHook;
import org.wargamer2010.signshop.hooks.WorldGuardHook;

public class SignShopChest {
    Block ssChest = null;

    public SignShopChest(Block bChest) {
        if(bChest.getType() == Material.CHEST)
            ssChest = bChest;
    }
    
    public Boolean allowedToLink(SignShopPlayer ssPlayer) {
        if(ssChest == null)
            return true;
        Boolean bAllowed = true;
        
        bAllowed = (bAllowed ? LocketteHook.canBuild(ssPlayer.getPlayer(), ssChest) : bAllowed);
        bAllowed = (bAllowed ? LWCHook.canBuild(ssPlayer.getPlayer(), ssChest) : bAllowed);
        bAllowed = (bAllowed ? WorldGuardHook.canBuild(ssPlayer.getPlayer(), ssChest) : bAllowed);
        
        return bAllowed;
    }
}

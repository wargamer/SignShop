package org.wargamer2010.signshop.operations;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Lever;
import org.bukkit.block.Block;
import org.wargamer2010.signshop.util.signshopUtil;
import org.wargamer2010.signshop.SignShop;

public class setRedstoneOff implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        Boolean foundLever = false;
        for(Block block : ssArgs.activatables)
            if(block.getType() == Material.LEVER)
                foundLever = true;
        if(!foundLever) {
            ssArgs.ssPlayer.sendMessage(SignShop.Errors.get("lever_missing"));
            return false;
        }
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        Boolean bReturn = false;
        Block bLever = null;
        for(int i = 0; i < ssArgs.activatables.size(); i++) {
            bLever = ssArgs.activatables.get(i);
            if(bLever.getType() == Material.LEVER) {
                BlockState state = bLever.getState();
                MaterialData data = state.getData();
                Lever lever = (Lever)data;
                if(lever.isPowered())
                    bReturn = true;
            }
        }
        if(!bReturn)
            ssArgs.ssPlayer.sendMessage(SignShop.Errors.get("already_off"));
        return bReturn;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Block bLever = null;
        
        for(int i = 0; i < ssArgs.activatables.size(); i++) {
            bLever = ssArgs.activatables.get(i);
            
            BlockState state = bLever.getState();
            MaterialData data = state.getData();                                        
            Lever lever = (Lever)data;                               
            if(lever.isPowered()) {
                lever.setPowered(false);                
                state.setData(lever);
                state.update();
                signshopUtil.generateInteractEvent(bLever, ssArgs.ssPlayer.getPlayer(), ssArgs.bfBlockFace);
            }
        }

        return true;
    }
}

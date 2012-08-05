package org.wargamer2010.signshop.operations;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Lever;
import org.bukkit.block.Block;
import org.bukkit.Bukkit;
import org.wargamer2010.signshop.util.signshopUtil;
import org.wargamer2010.signshop.util.lagSetter;
import org.wargamer2010.signshop.SignShop;

public class setRedStoneOnTemp implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        Boolean foundLever = false;
        for(Block block : ssArgs.get_activatables())
            if(block.getType() == Material.LEVER)
                foundLever = true;
        if(!foundLever) {            
            ssArgs.get_ssPlayer().sendMessage(signshopUtil.getError("lever_missing", ssArgs.messageParts));
            return false;
        }
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {        
        Boolean bReturn = false;
        Block bLever = null; 
        
        for(int i = 0; i < ssArgs.get_activatables().size(); i++) {            
            bLever = ssArgs.get_activatables().get(i);
            
            if(bLever.getType() == Material.LEVER) {                
                BlockState state = bLever.getState();
                MaterialData data = state.getData();
                Lever lever = (Lever)data;
                if(!lever.isPowered())
                    bReturn = true;
            }
        }
        if(!bReturn)
            ssArgs.get_ssPlayer().sendMessage(signshopUtil.getError("already_on", ssArgs.messageParts));
        return bReturn;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Block bLever = null;
        
        Integer delay = 20;
        if(ssArgs.operationParameters.size() > 0) {            
            try {
                delay = Integer.parseInt(ssArgs.operationParameters.get(0));
                if(delay <= 0)
                    delay = 20;
            } catch(NumberFormatException ex) {
                
            }
        }
        
        for(int i = 0; i < ssArgs.get_activatables().size(); i++) {
            bLever = ssArgs.get_activatables().get(i);
            if(bLever.getType() == Material.LEVER) {
                BlockState state = bLever.getState();
                MaterialData data = state.getData();
                Lever lever = (Lever)data;
                if(!lever.isPowered()) {
                    lever.setPowered(true);                
                    state.setData(lever);
                    state.update();
                    signshopUtil.generateInteractEvent(bLever, ssArgs.get_ssPlayer().getPlayer(), ssArgs.get_bfBlockFace());
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SignShop.getInstance(),new lagSetter(bLever),10*delay);
                }            
            }
        }    

        return true;
    }
}

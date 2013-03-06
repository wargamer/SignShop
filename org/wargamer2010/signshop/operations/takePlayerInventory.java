package org.wargamer2010.signshop.operations;

import org.bukkit.entity.Player;

public class takePlayerInventory implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {        
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {                        
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {                
        Player player = ssArgs.getPlayer().get().getPlayer();
        if(ssArgs.isOperationParameter("clearArmor")) {
            player.getInventory().setArmorContents(null);
        }
        player.getInventory().clear();        
        return true;
    }
}

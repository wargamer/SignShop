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
        Player player = ssArgs.get_ssPlayer().getPlayer();
        if(!ssArgs.operationParameters.isEmpty() && ssArgs.operationParameters.get(0).equals("clearArmor")) {
            player.getInventory().setArmorContents(null);
        }
        player.getInventory().clear();        
        return true;
    }
}

package org.wargamer2010.signshop.operations;

import com.earth2me.essentials.craftbukkit.SetExpFix;
import org.wargamer2010.signshop.util.signshopUtil;

public class givePlayerXP implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        Float XP = signshopUtil.getNumberFromThirdLine(ssArgs.get_bSign());
        if(XP == 0.0) {
            ssArgs.get_ssPlayer().sendMessage("Please put the amount of XP you'd like to Sell on the third line!");
            return false;
        }
        ssArgs.setMessagePart("!xp", XP.toString());
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {        
        if(ssArgs.get_ssPlayer().getPlayer() == null)
            return true;
        Float XP = signshopUtil.getNumberFromThirdLine(ssArgs.get_bSign());
        if(XP == 0.0) {
            ssArgs.get_ssPlayer().sendMessage("Invalid amount of XP on the third line!");
            return false;
        }
        ssArgs.setMessagePart("!xp", XP.toString());
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Float XP = signshopUtil.getNumberFromThirdLine(ssArgs.get_bSign());
        if(!ssArgs.operationParameters.isEmpty() && ssArgs.operationParameters.get(0).equals("raw")) {            
            ssArgs.get_ssPlayer().getPlayer().giveExp(XP.intValue());                        
            ssArgs.setMessagePart("!hasxp", ((Integer)SetExpFix.getTotalExperience(ssArgs.get_ssPlayer().getPlayer())).toString());
        } else {
            Integer setLevel = (ssArgs.get_ssPlayer().getPlayer().getLevel() + XP.intValue());
            ssArgs.get_ssPlayer().getPlayer().setLevel(setLevel);
            ssArgs.setMessagePart("!hasxp", setLevel.toString());
        }
        ssArgs.setMessagePart("!xp", XP.toString());
        return true;
    }
}

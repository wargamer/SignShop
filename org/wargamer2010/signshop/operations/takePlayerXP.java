package org.wargamer2010.signshop.operations;

import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.essentials.SetExpFix;
import org.wargamer2010.signshop.util.signshopUtil;

public class takePlayerXP implements SignShopOperation {    
       
    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        Float XP = signshopUtil.getNumberFromThirdLine(ssArgs.get_bSign());
        if(XP == 0.0) {
            ssArgs.get_ssPlayer().sendMessage("Please put the amount of XP you'd like players to Buy on the third line!");
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
        Integer refXP = 0;        
        if(ssArgs.isOperationParameter("raw"))
            refXP = SetExpFix.getTotalExperience(ssArgs.get_ssPlayer().getPlayer());
        else
            refXP = ssArgs.get_ssPlayer().getPlayer().getLevel();
        ssArgs.setMessagePart("!hasxp", refXP.toString());
        if(refXP < XP) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("no_player_xp", ssArgs.messageParts));
            return false;
        }        
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Float XP = signshopUtil.getNumberFromThirdLine(ssArgs.get_bSign());        
        Integer setAmount = 0;
        
        if(ssArgs.isOperationParameter("raw")) {
            setAmount = (SetExpFix.getTotalExperience(ssArgs.get_ssPlayer().getPlayer()) - XP.intValue());                        
            SetExpFix.setTotalExperience(ssArgs.get_ssPlayer().getPlayer(), setAmount);            
        } else {
            setAmount = (ssArgs.get_ssPlayer().getPlayer().getLevel() - XP.intValue());
            ssArgs.get_ssPlayer().getPlayer().setLevel(setAmount);
        }
        
        ssArgs.setMessagePart("!hasxp", setAmount.toString());
        ssArgs.setMessagePart("!xp", XP.toString());        
        return true;
    }
}

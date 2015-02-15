package org.wargamer2010.signshop.operations;

import org.wargamer2010.essentials.SetExpFix;
import org.wargamer2010.signshop.util.signshopUtil;

public class givePlayerXP implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        Double XP = signshopUtil.getNumberFromThirdLine(ssArgs.getSign().get());
        if(XP == 0.0) {
            ssArgs.getPlayer().get().sendMessage("Please put the amount of XP you'd like to Sell on the third line!");
            return false;
        }
        ssArgs.setMessagePart("!xp", XP.toString());
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(!ssArgs.isPlayerOnline())
            return true;
        Double XP = signshopUtil.getNumberFromThirdLine(ssArgs.getSign().get());
        if(XP == 0.0) {
            ssArgs.getPlayer().get().sendMessage("Invalid amount of XP on the third line!");
            return false;
        }
        ssArgs.setMessagePart("!xp", XP.toString());
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Double XP = signshopUtil.getNumberFromThirdLine(ssArgs.getSign().get());
        if(ssArgs.isOperationParameter("raw")) {
            ssArgs.getPlayer().get().getPlayer().giveExp(XP.intValue());
            ssArgs.setMessagePart("!hasxp", ((Integer)SetExpFix.getTotalExperience(ssArgs.getPlayer().get().getPlayer())).toString());
        } else {
            Integer setLevel = (ssArgs.getPlayer().get().getPlayer().getLevel() + XP.intValue());
            ssArgs.getPlayer().get().getPlayer().setLevel(setLevel);
            ssArgs.setMessagePart("!hasxp", setLevel.toString());
        }
        ssArgs.setMessagePart("!xp", XP.toString());
        return true;
    }
}

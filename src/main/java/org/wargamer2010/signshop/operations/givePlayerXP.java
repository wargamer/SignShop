package org.wargamer2010.signshop.operations;

import org.wargamer2010.essentials.SetExpFix;
import org.wargamer2010.signshop.util.signshopUtil;

/**
 * Operation that grants experience points to the player.
 * Amount is specified on sign line 3, supports both raw XP and level-based modes.
 */
public class givePlayerXP implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        double XP = signshopUtil.getNumberFromThirdLine(ssArgs.getSign().get());
        if(XP == 0.0) {
            ssArgs.getPlayer().get().sendMessage("Please put the amount of XP you'd like to Sell on the third line!");
            return false;
        }
        ssArgs.setMessagePart("!xp", Double.toString(XP));
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(!ssArgs.isPlayerOnline())
            return true;
        double XP = signshopUtil.getNumberFromThirdLine(ssArgs.getSign().get());
        if(XP == 0.0) {
            ssArgs.getPlayer().get().sendMessage("Invalid amount of XP on the third line!");
            return false;
        }
        ssArgs.setMessagePart("!xp", Double.toString(XP));
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Double XP = signshopUtil.getNumberFromThirdLine(ssArgs.getSign().get());
        if(ssArgs.isOperationParameter("raw")) {
            ssArgs.getPlayer().get().getPlayer().giveExp(XP.intValue());
            ssArgs.setMessagePart("!hasxp", ((Integer)SetExpFix.getTotalExperience(ssArgs.getPlayer().get().getPlayer())).toString());
        } else {
            int setLevel = (ssArgs.getPlayer().get().getPlayer().getLevel() + XP.intValue());
            ssArgs.getPlayer().get().getPlayer().setLevel(setLevel);
            ssArgs.setMessagePart("!hasxp", Integer.toString(setLevel));
        }
        ssArgs.setMessagePart("!xp", XP.toString());
        return true;
    }
}

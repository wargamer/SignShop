package org.wargamer2010.signshop.operations;

import org.wargamer2010.essentials.SetExpFix;
import org.wargamer2010.signshop.util.signshopUtil;

public class takePlayerXP implements SignShopOperation {


    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        Double XP = signshopUtil.getNumberFromThirdLine(ssArgs.getSign().get());
        if(XP == 0.0) {
            ssArgs.getPlayer().get().sendMessage("Please put the amount of XP you'd like players to Buy on the third line!");
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
        Integer refXP;
        if(ssArgs.isOperationParameter("raw"))
            refXP = SetExpFix.getTotalExperience(ssArgs.getPlayer().get().getPlayer());
        else
            refXP = ssArgs.getPlayer().get().getPlayer().getLevel();
        ssArgs.setMessagePart("!hasxp", refXP.toString());
        if(refXP < XP) {
            ssArgs.sendFailedRequirementsMessage("no_player_xp");
            return false;
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Double XP = signshopUtil.getNumberFromThirdLine(ssArgs.getSign().get());
        Integer setAmount;

        if(ssArgs.isOperationParameter("raw")) {
            setAmount = (SetExpFix.getTotalExperience(ssArgs.getPlayer().get().getPlayer()) - XP.intValue());
            SetExpFix.setTotalExperience(ssArgs.getPlayer().get().getPlayer(), setAmount);
        } else {
            setAmount = (ssArgs.getPlayer().get().getPlayer().getLevel() - XP.intValue());
            ssArgs.getPlayer().get().getPlayer().setLevel(setAmount);
        }

        ssArgs.setMessagePart("!hasxp", setAmount.toString());
        ssArgs.setMessagePart("!xp", XP.toString());
        return true;
    }
}

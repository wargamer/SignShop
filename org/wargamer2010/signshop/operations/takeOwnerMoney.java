package org.wargamer2010.signshop.operations;

import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.util.signshopUtil;

public class takeOwnerMoney implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        ssArgs.setMessagePart("!price", economyUtil.formatMoney(ssArgs.get_fPrice()));
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(ssArgs.get_ssPlayer().getPlayer() == null)
            return true;
        Float fPrice = signshopUtil.ApplyPriceMod(ssArgs);
        if(!ssArgs.get_ssOwner().hasMoney(fPrice)) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("no_shop_money", ssArgs.messageParts));
            return false;
        }
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Float fPrice = signshopUtil.ApplyPriceMod(ssArgs);
        Boolean bTransaction = ssArgs.get_ssOwner().mutateMoney(-fPrice);
        if(!bTransaction)
            ssArgs.get_ssPlayer().sendMessage("The money transaction failed, please contact the System Administrator");
        return bTransaction;
    }
}

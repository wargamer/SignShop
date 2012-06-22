package org.wargamer2010.signshop.operations;

import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.economyUtil;

public class takeOwnerMoney implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(ssArgs.ssPlayer.getPlayer() == null)
            return true;
        Float fPricemod = ssArgs.ssPlayer.getPlayerPricemod(ssArgs.sOperation, false);
        Float fPrice = (ssArgs.fPrice * fPricemod);
        if(!ssArgs.ssOwner.hasMoney(fPrice)) {
            ssArgs.ssPlayer.sendMessage(SignShop.Errors.get("no_shop_money").replace("!price", economyUtil.formatMoney(fPrice)));
            return false;
        }
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Float fPricemod = ssArgs.ssPlayer.getPlayerPricemod(ssArgs.sOperation, false);
        Float fPrice = (ssArgs.fPrice * fPricemod);
        Boolean bTransaction = ssArgs.ssOwner.mutateMoney(-fPrice);
        if(!bTransaction)
            ssArgs.ssPlayer.sendMessage("The money transaction failed, please contact the System Administrator");
        return bTransaction;
    }
}

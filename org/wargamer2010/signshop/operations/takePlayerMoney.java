package org.wargamer2010.signshop.operations;

import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.economyUtil;

public class takePlayerMoney implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {                
        Float fPricemod = ssArgs.ssPlayer.getPlayerPricemod(ssArgs.sOperation, true);
        Float fPrice = (ssArgs.fPrice * fPricemod);        
        if(!ssArgs.ssPlayer.hasMoney(fPrice)) {
            ssArgs.ssPlayer.sendMessage(SignShop.Errors.get("no_player_money").replace("!price",economyUtil.formatMoney(fPrice)));
            return false;
        }        
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Float fPricemod = ssArgs.ssPlayer.getPlayerPricemod(ssArgs.sOperation, true);
        Float fPrice = (ssArgs.fPrice * fPricemod);
        Boolean bTransaction = ssArgs.ssPlayer.mutateMoney(-fPrice);
        if(!bTransaction)
            ssArgs.ssPlayer.sendMessage("The money transaction failed, please contact the System Administrator");
        return bTransaction;
    }
}

package org.wargamer2010.signshop.operations;

import org.wargamer2010.signshop.util.signshopUtil;
import org.wargamer2010.signshop.util.economyUtil;

public class takePlayerMoney implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        ssArgs.setMessagePart("!price", economyUtil.formatMoney(ssArgs.get_fPrice()));
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {                
        Float fPricemod = ssArgs.get_ssPlayer().getPlayerPricemod(ssArgs.get_sOperation(), true);
        Float fPrice = (ssArgs.get_fPrice() * fPricemod);        
        ssArgs.set_fPrice(fPrice);
        ssArgs.setMessagePart("!price", economyUtil.formatMoney(fPrice));        
        if(!ssArgs.get_ssPlayer().hasMoney(fPrice)) {
            ssArgs.get_ssPlayer().sendMessage(signshopUtil.getError("no_player_money", ssArgs.messageParts));            
            return false;
        }
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Float fPricemod = ssArgs.get_ssPlayer().getPlayerPricemod(ssArgs.get_sOperation(), true);
        Float fPrice = (ssArgs.get_fPrice() * fPricemod);
        Boolean bTransaction = ssArgs.get_ssPlayer().mutateMoney(-fPrice);
        if(!bTransaction)
            ssArgs.get_ssPlayer().sendMessage("The money transaction failed, please contact the System Administrator");
        return bTransaction;
    }
}

package org.wargamer2010.signshop.operations;

import org.wargamer2010.signshop.util.economyUtil;

public class givePlayerMoney implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        ssArgs.setMessagePart("!price", economyUtil.formatMoney(ssArgs.get_fPrice()));
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {        
        ssArgs.set_fPrice(ssArgs.get_fPrice());
        ssArgs.setMessagePart("!price", economyUtil.formatMoney(ssArgs.get_fPrice()));
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Float fPricemod = ssArgs.get_ssPlayer().getPlayerPricemod(ssArgs.get_sOperation(), false);
        Float fPrice = (ssArgs.get_fPrice() * fPricemod);
        Boolean bTransaction = ssArgs.get_ssPlayer().mutateMoney(fPrice);
        if(!bTransaction)
            ssArgs.get_ssPlayer().sendMessage("The money transaction failed, please contact the System Administrator");
        return bTransaction;
    }
}

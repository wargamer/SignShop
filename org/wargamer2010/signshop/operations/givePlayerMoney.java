package org.wargamer2010.signshop.operations;

import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.signshop.util.signshopUtil;

public class givePlayerMoney implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        ssArgs.setMessagePart("!price", economyUtil.formatMoney(ssArgs.get_fPrice()));
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {                
        signshopUtil.ApplyPriceMod(ssArgs);
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Float fPrice = signshopUtil.ApplyPriceMod(ssArgs);
        Boolean bTransaction = ssArgs.get_ssPlayer().mutateMoney(fPrice);
        if(!bTransaction)
            ssArgs.get_ssPlayer().sendMessage("The money transaction failed, please contact the System Administrator");
        return bTransaction;
    }
}

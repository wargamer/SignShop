package org.wargamer2010.signshop.operations;

import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.economyUtil;

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
        Float fPricemod = ssArgs.get_ssPlayer().getPlayerPricemod(ssArgs.get_sOperation(), false);
        Float fPrice = (ssArgs.get_fPrice() * fPricemod);
        if(!ssArgs.get_ssOwner().hasMoney(fPrice)) {
            ssArgs.get_ssPlayer().sendMessage(SignShop.Errors.get("no_shop_money").replace("!price", economyUtil.formatMoney(fPrice)));
            return false;
        }
        ssArgs.set_fPrice(fPrice);
        ssArgs.setMessagePart("!price", economyUtil.formatMoney(fPrice));
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Float fPricemod = ssArgs.get_ssPlayer().getPlayerPricemod(ssArgs.get_sOperation(), false);
        Float fPrice = (ssArgs.get_fPrice() * fPricemod);
        Boolean bTransaction = ssArgs.get_ssOwner().mutateMoney(-fPrice);
        if(!bTransaction)
            ssArgs.get_ssPlayer().sendMessage("The money transaction failed, please contact the System Administrator");
        return bTransaction;
    }
}

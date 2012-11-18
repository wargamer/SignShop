package org.wargamer2010.signshop.operations;

import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.signshop.util.signshopUtil;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.configuration.Storage;

public class giveOwnerMoney implements SignShopOperation {
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
        Seller seller = Storage.get().getSeller(ssArgs.get_bSign().getLocation());
        Boolean bTransaction = false;
        if(seller != null && seller.getMisc().containsKey("sharesigns")) {
            bTransaction = signshopUtil.distributeMoney(seller, fPrice, ssArgs.get_ssPlayer());
        } else {
            bTransaction = ssArgs.get_ssOwner().mutateMoney(fPrice);
        }
        if(!bTransaction)
            ssArgs.get_ssPlayer().sendMessage("The money transaction failed, please contact the System Administrator");
        return bTransaction;
    }
}

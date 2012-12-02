package org.wargamer2010.signshop.operations;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.listeners.SignShopWorthListener;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

public class getPriceFromWorth implements SignShopOperation {

    private Float getTotalPrice(final ItemStack[] pStacks) {
        if(!SignShopWorthListener.essLoaded())
            return -1.0f;
        Float fTotal = 0.0f;
        for(ItemStack stack : pStacks) {
            Double dTemp = SignShopWorthListener.getPrice(stack);
            if(dTemp > 0.0d) {
                fTotal += (dTemp.floatValue() * stack.getAmount());
            }
        }
        return fTotal;
    }

    private boolean signHasPlaceholder(Block bSign) {
        if(!itemUtil.clickedSign(bSign))
            return false;
        Sign sign = (Sign)bSign.getState();
        if(sign.getLine(3) != null && sign.getLine(3).equalsIgnoreCase("[worth]"))
            return true;
        return false;
    }

    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(!signHasPlaceholder(ssArgs.get_bSign()))
            return true;
        Float fTotal = getTotalPrice(ssArgs.get_isItems());
        if(fTotal >= 0.0f) {
            ssArgs.set_fPrice(fTotal);
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("price_drawn_from_essentials", ssArgs.messageParts));
        }
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        return runOperation(ssArgs);
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        if(!signHasPlaceholder(ssArgs.get_bSign()))
            return true;
        Float fTotal = getTotalPrice(ssArgs.get_isItems());
        if(fTotal >= 0.0f)
            ssArgs.set_fPrice(fTotal);
        signshopUtil.ApplyPriceMod(ssArgs);
        return true;
    }
}

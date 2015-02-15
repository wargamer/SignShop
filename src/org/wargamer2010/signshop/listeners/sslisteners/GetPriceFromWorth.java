package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.events.SSMoneyEventType;
import org.wargamer2010.signshop.events.SSMoneyRequestType;
import org.wargamer2010.signshop.events.SSMoneyTransactionEvent;
import org.wargamer2010.signshop.listeners.SignShopWorthListener;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.signshop.util.itemUtil;

public class GetPriceFromWorth implements Listener {

    private double getTotalPrice(final ItemStack[] pStacks) {
        if(!SignShopWorthListener.essLoaded())
            return -1.0f;
        double fTotal = 0.0f;
        for(ItemStack stack : pStacks) {
            double dTemp = SignShopWorthListener.getPrice(stack);
            if(dTemp > 0.0d) {
                fTotal += (dTemp * stack.getAmount());
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


    private double adjustPrice(Block sign, ItemStack[] items, SignShopPlayer player, String sOperation, SSMoneyEventType type) {
        double returnValue = -1.0d;
        if(!SignShopConfig.getEnablePriceFromWorth() || !signHasPlaceholder(sign))
            return returnValue;
        returnValue = getTotalPrice(items);
        return returnValue;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSSMoneyTransactionEvent(SSMoneyTransactionEvent event) {
        if(event.isCancelled() || event.getItems() == null)
            return;
        double newPrice = this.adjustPrice(event.getSign(), event.getItems(), event.getPlayer(), event.getOperation(), event.getTransactionType());
        if(newPrice > -1.0f) {
            if(event.getRequestType() == SSMoneyRequestType.GetAmount)
                event.getPlayer().sendMessage(SignShopConfig.getError("price_drawn_from_essentials", null));
            event.setPrice(newPrice);
            if(event.getArguments() != null)
                event.getArguments().resetPriceMod();
            event.setMessagePart("!price", economyUtil.formatMoney(newPrice));
        }
    }
}

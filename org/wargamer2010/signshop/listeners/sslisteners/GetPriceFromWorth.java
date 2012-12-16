package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.events.SSPostTransactionEvent;
import org.wargamer2010.signshop.events.SSPreTransactionEvent;
import org.wargamer2010.signshop.listeners.SignShopWorthListener;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

public class GetPriceFromWorth implements Listener {

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


    private Float adjustPrice(Block sign, ItemStack[] items, SignShopPlayer player, String sOperation) {
        Float returnValue = -1.0f;
        if(!SignShopConfig.getEnablePriceFromWorth() || !signHasPlaceholder(sign))
            return returnValue;
        returnValue = getTotalPrice(items);
        returnValue = signshopUtil.ApplyPriceMod(player, returnValue, sOperation);
        return returnValue;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSBuildEvent(SSCreatedEvent event) {
        if(event.isCancelled())
            return;
        Float newPrice = this.adjustPrice(event.getSign(), event.getItems(), event.getPlayer(), event.getOperation());
        if(newPrice > -1.0f) {
            event.getPlayer().sendMessage(SignShopConfig.getError("price_drawn_from_essentials", null));
            event.setPrice(newPrice);
            event.setMessagePart("!price", newPrice.toString());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSPreTransactionEvent(SSPreTransactionEvent event) {
        if(event.isCancelled())
            return;
        Float newPrice = this.adjustPrice(event.getSign(), event.getItems(), event.getPlayer(), event.getOperation());
        if(newPrice > -1.0f) {
            event.setPrice(newPrice);
            event.setMessagePart("!price", newPrice.toString());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSPostTransactionEvent(SSPostTransactionEvent event) {
        if(event.isCancelled())
            return;
        Float newPrice = this.adjustPrice(event.getSign(), event.getItems(), event.getPlayer(), event.getOperation());
        if(newPrice > -1.0f) {
            event.setPrice(newPrice);
            event.setMessagePart("!price", newPrice.toString());
        }
    }
}

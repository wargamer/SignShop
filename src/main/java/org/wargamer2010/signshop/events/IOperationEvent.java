
package org.wargamer2010.signshop.events;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.SignShopPlayer;

import java.util.Map;

public interface IOperationEvent extends Cancellable, IMessagePartContainer {
    SignShopPlayer getPlayer();

    Seller getShop();

    double getPrice();

    void setPrice(double pAmount);

    Block getSign();

    String getOperation();

    ItemStack[] getItems();

    @Override
    Map<String, Object> getMessageParts();

    @Override
    void setMessagePart(String part, Object value);
}

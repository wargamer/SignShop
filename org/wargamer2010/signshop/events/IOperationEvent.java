
package org.wargamer2010.signshop.events;

import java.util.Map;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.SignShopPlayer;

public interface IOperationEvent extends Cancellable {
    public SignShopPlayer getPlayer();

    public Seller getShop();

    public double getPrice();

    public void setPrice(double pAmount);

    public Block getSign();

    public String getOperation();

    public ItemStack[] getItems();

    public Map<String, String> getMessageParts();

    public void setMessagePart(String part, String value);
}

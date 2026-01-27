package org.wargamer2010.signshop.events;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.wargamer2010.signshop.player.SignShopPlayer;

import java.util.List;
import java.util.Map;

/**
 * Event fired when a new SignShop is created.
 *
 * <p>Cancelling this event prevents the shop from being created. Listeners can
 * modify price, items, or misc settings before creation is finalized.</p>
 */
public class SSCreatedEvent extends SSEvent {
    private static final HandlerList handlers = new HandlerList();

    private double fPrice;
    private final ItemStack[] isItems;
    private final List<Block> containables;
    private final List<Block> activatables;
    private final SignShopPlayer ssPlayer;
    private final Block bSign;
    private final String sOperation;
    private final Map<String, Object> messageParts;
    private final Map<String, String> miscSettings;


    public SSCreatedEvent(double pPrice, ItemStack[] pItems, List<Block> pContainables, List<Block> pActivatables, SignShopPlayer pPlayer, Block pSign, String pOperation, Map<String, Object> pMessageParts, Map<String, String> pMisc) {
        fPrice = pPrice;
        isItems = pItems;
        containables = pContainables;
        activatables = pActivatables;
        ssPlayer = pPlayer;
        bSign = pSign;
        sOperation = pOperation;
        messageParts = pMessageParts;
        miscSettings = pMisc;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public double getPrice() {
        return fPrice;
    }

    public void setPrice(double fPrice) {
        this.fPrice = fPrice;
    }

    public ItemStack[] getItems() {
        return isItems;
    }

    public List<Block> getContainables() {
        return containables;
    }

    public List<Block> getActivatables() {
        return activatables;
    }

    public SignShopPlayer getPlayer() {
        return ssPlayer;
    }

    public Block getSign() {
        return bSign;
    }

    public String getOperation() {
        return sOperation;
    }

    @Override
    public Map<String, Object> getMessageParts() {
        return messageParts;
    }

    @Override
    public void setMessagePart(String part, Object value) {
        messageParts.put(part, value);
    }

    /**
     * Overloaded method for binary compatibility with external plugins.
     *
     * @param part The message part key
     * @param value The string value
     */
    public void setMessagePart(String part, String value) {
        setMessagePart(part, (Object) value);
    }

    public Map<String, String> getMiscSettings() {
        return miscSettings;
    }

    public void setMiscSetting(String setting, String value) {
        miscSettings.put(setting, value);
    }
}

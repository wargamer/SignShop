package org.wargamer2010.signshop.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class SSCreatedEvent extends SSEvent {
    private static final HandlerList handlers = new HandlerList();

    private double fPrice = -1.0f;
    private ItemStack[] isItems = null;
    private List<Block> containables = null;
    private List<Block> activatables = null;
    private SignShopPlayer ssPlayer = null;
    private Block bSign = null;
    private String sOperation = "";
    private Map<String, String> messageParts = new HashMap<String, String>();
    private Map<String, String> miscSettings = new HashMap<String, String>();


    public SSCreatedEvent(double pPrice, ItemStack[] pItems, List<Block> pContainables, List<Block> pActivatables, SignShopPlayer pPlayer, Block pSign, String pOperation, Map<String, String> pMessageParts, Map<String, String> pMisc) {
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
    public Map<String, String> getMessageParts() {
        return messageParts;
    }

    @Override
    public void setMessagePart(String part, String value) {
        messageParts.put(part, value);
    }

    public Map<String, String> getMiscSettings() {
        return miscSettings;
    }

    public void setMiscSetting(String setting, String value) {
        miscSettings.put(setting, value);
    }
}

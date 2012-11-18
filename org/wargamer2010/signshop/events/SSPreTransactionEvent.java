package org.wargamer2010.signshop.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class SSPreTransactionEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean bCancelled = false;

    private Float fPrice = -1.0f;
    private ItemStack[] isItems = null;
    private List<Block> containables = null;
    private List<Block> activatables = null;
    private SignShopPlayer ssPlayer = null;
    private SignShopPlayer ssOwner = null;
    private Block bSign = null;
    private String sOperation = "";
    private Seller seShop = null;
    private Map<String, String> messageParts = new HashMap<String, String>();

    public SSPreTransactionEvent(Float pPrice,
                                ItemStack[] pItems,
                                List<Block> pContainables,
                                List<Block> pActivatables,
                                SignShopPlayer pPlayer,
                                SignShopPlayer pOwner,
                                Block pSign,
                                String pOperation,
                                Map<String, String> pMessageParts,
                                Seller pShop) {
        fPrice = pPrice;
        isItems = pItems;
        containables = pContainables;
        activatables = pActivatables;
        ssPlayer = pPlayer;
        ssOwner = pOwner;
        bSign = pSign;
        sOperation = pOperation;
        messageParts = pMessageParts;
        seShop = pShop;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return bCancelled;
    }

    @Override
    public void setCancelled(boolean pCancelled) {
        bCancelled = pCancelled;
    }

    public Float getPrice() {
        return fPrice;
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

    public SignShopPlayer getOwner() {
        return ssOwner;
    }

    public Seller getShop() {
        return seShop;
    }

    public Block getSign() {
        return bSign;
    }

    public String getOperation() {
        return sOperation;
    }

    public Map<String, String> getMessageParts() {
        return messageParts;
    }
}

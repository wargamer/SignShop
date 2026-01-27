package org.wargamer2010.signshop.events;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.SignShopPlayer;

import java.util.List;
import java.util.Map;

/**
 * Event fired before a shop transaction is executed.
 *
 * <p>Cancelling this event prevents the transaction. Listeners can modify
 * the price, apply discounts, or add additional validation logic.</p>
 */
public class SSPreTransactionEvent extends SSEvent implements IOperationEvent {
    private static final HandlerList handlers = new HandlerList();

    private double fPrice;
    private final ItemStack[] isItems;
    private final List<Block> containables;
    private final List<Block> activatables;
    private final SignShopPlayer ssPlayer;
    private final SignShopPlayer ssOwner;
    private final Block bSign;
    private final String sOperation;
    private final Seller seShop;
    private final Action aAction;
    private final boolean bRequirementsOK;

    public SSPreTransactionEvent(double pPrice,
                                ItemStack[] pItems,
                                List<Block> pContainables,
                                List<Block> pActivatables,
                                SignShopPlayer pPlayer,
                                SignShopPlayer pOwner,
                                Block pSign,
                                String pOperation,
                                Map<String, Object> pMessageParts,
                                Seller pShop,
                                Action pAction,
                                boolean pRequirementsOK) {
        super(pMessageParts);
        fPrice = pPrice;
        isItems = pItems;
        containables = pContainables;
        activatables = pActivatables;
        ssPlayer = pPlayer;
        ssOwner = pOwner;
        bSign = pSign;
        sOperation = pOperation;
        seShop = pShop;
        aAction = pAction;
        bRequirementsOK = pRequirementsOK;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public double getPrice() {
        return fPrice;
    }

    @Override
    public void setPrice(double fPrice) {
        this.fPrice = fPrice;
    }

    @Override
    public ItemStack[] getItems() {
        return isItems;
    }

    public List<Block> getContainables() {
        return containables;
    }

    public List<Block> getActivatables() {
        return activatables;
    }

    @Override
    public SignShopPlayer getPlayer() {
        return ssPlayer;
    }

    public SignShopPlayer getOwner() {
        return ssOwner;
    }

    @Override
    public Seller getShop() {
        return seShop;
    }

    @Override
    public Block getSign() {
        return bSign;
    }

    @Override
    public String getOperation() {
        return sOperation;
    }

    public Action getAction() {
        return aAction;
    }

    public boolean getRequirementsOK() {
        return bRequirementsOK;
    }
}

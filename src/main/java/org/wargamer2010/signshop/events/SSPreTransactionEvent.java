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

public class SSPreTransactionEvent extends SSEvent implements IOperationEvent {
    private static final HandlerList handlers = new HandlerList();

    private double fPrice;
    private ItemStack[] isItems;
    private List<Block> containables;
    private List<Block> activatables;
    private SignShopPlayer ssPlayer;
    private SignShopPlayer ssOwner;
    private Block bSign;
    private String sOperation;
    private Seller seShop;
    private Action aAction;
    private boolean bRequirementsOK;

    public SSPreTransactionEvent(double pPrice,
                                ItemStack[] pItems,
                                List<Block> pContainables,
                                List<Block> pActivatables,
                                SignShopPlayer pPlayer,
                                SignShopPlayer pOwner,
                                Block pSign,
                                String pOperation,
                                Map<String, String> pMessageParts,
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

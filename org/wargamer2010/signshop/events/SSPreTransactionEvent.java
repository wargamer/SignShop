package org.wargamer2010.signshop.events;

import java.util.List;
import java.util.Map;
import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class SSPreTransactionEvent extends SSEvent implements IOperationEvent {
    private static final HandlerList handlers = new HandlerList();

    private double fPrice = -1.0f;
    private ItemStack[] isItems = null;
    private List<Block> containables = null;
    private List<Block> activatables = null;
    private SignShopPlayer ssPlayer = null;
    private SignShopPlayer ssOwner = null;
    private Block bSign = null;
    private String sOperation = "";
    private Seller seShop = null;
    private Action aAction = null;
    private boolean bRequirementsOK = true;

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

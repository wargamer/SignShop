package org.wargamer2010.signshop.events;

import java.util.Map;
import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class SSMoneyTransactionEvent extends SSEvent implements IOperationEvent {
    private static final HandlerList handlers = new HandlerList();

    private SignShopPlayer ssPlayer = null;
    private Block bBlock = null;
    private Seller seShop = null;
    private double fAmount;
    private Block bSign = null;
    private String sOperation = "";
    private ItemStack[] isItems = null;
    private boolean bLeftClicking = false;
    private SSMoneyEventType meType = SSMoneyEventType.Unknown;
    private SSMoneyRequestType rtRequestType = SSMoneyRequestType.Unknown;
    private boolean bHandled = false;
    private SignShopArguments ssArgs = null;

    public SSMoneyTransactionEvent(SignShopPlayer pPlayer, Seller pShop, double pAmount, Block pSign, String pOperation, ItemStack[] pItems,
            boolean leftClicking, SSMoneyEventType pType, Map<String, String> pMessageParts, SSMoneyRequestType pRequestType) {
        super(pMessageParts);
        ssPlayer = pPlayer;
        seShop = pShop;
        fAmount = pAmount;
        bSign = pSign;
        sOperation = pOperation;
        isItems = pItems;
        bLeftClicking = leftClicking;
        meType = pType;
        rtRequestType = pRequestType;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public SignShopPlayer getPlayer() {
        return ssPlayer;
    }

    public Block getBlock() {
        return bBlock;
    }

    @Override
    public Seller getShop() {
        return seShop;
    }

    @Override
    public double getPrice() {
        return fAmount;
    }

    @Override
    public void setPrice(double pAmount) {
        fAmount = pAmount;
    }

    @Override
    public Block getSign() {
        return bSign;
    }

    @Override
    public String getOperation() {
        return sOperation;
    }

    @Override
    public ItemStack[] getItems() {
        return isItems;
    }

    public boolean isLeftClicking() {
        return bLeftClicking;
    }

    public SignShopArguments getArguments() {
        return ssArgs;
    }

    public void setArguments(SignShopArguments ssArgs) {
        this.ssArgs = ssArgs;
    }


    public SSMoneyEventType getTransactionType() {
        return meType;
    }

    public SSMoneyRequestType getRequestType() {
        return rtRequestType;
    }

    public boolean isBalanceOrExecution() {
        return rtRequestType == SSMoneyRequestType.CheckBalance || rtRequestType == SSMoneyRequestType.ExecuteTransaction;
    }

    public boolean isHandled() {
        return bHandled;
    }

    public void setHandled(boolean pHandled) {
        bHandled = pHandled;
    }

    public void sendFailedRequirementsMessage(String messageName) {
        if(!isLeftClicking())
            getPlayer().sendMessage(SignShopConfig.getError(messageName, getMessageParts()));
    }
}

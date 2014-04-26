package org.wargamer2010.signshop.events;

import java.util.Map;
import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class SSMoneyTransactionEvent extends SSEvent {
    private static final HandlerList handlers = new HandlerList();

    private SignShopPlayer ssPlayer = null;
    private Block bBlock = null;
    private Seller seShop = null;
    private Float fAmount;
    private Block bSign = null;
    private String sOperation = "";
    private ItemStack[] isItems = null;
    private boolean bLeftClicking = false;
    private SSMoneyEventType meType = SSMoneyEventType.Unknown;
    private boolean bCheckOnly = false;
    private boolean bHandled = false;

    public SSMoneyTransactionEvent(SignShopPlayer pPlayer, Seller pShop, Float pAmount, Block pSign, String pOperation, ItemStack[] pItems,
            boolean leftClicking, SSMoneyEventType pType, Map<String, String> pMessageParts, boolean pCheckOnly) {
        super(pMessageParts);
        ssPlayer = pPlayer;
        seShop = pShop;
        fAmount = pAmount;
        bSign = pSign;
        sOperation = pOperation;
        isItems = pItems;
        bLeftClicking = leftClicking;
        meType = pType;
        bCheckOnly = pCheckOnly;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public SignShopPlayer getPlayer() {
        return ssPlayer;
    }

    public Block getBlock() {
        return bBlock;
    }

    public Seller getShop() {
        return seShop;
    }

    public Float getAmount() {
        return fAmount;
    }

    public void setAmount(Float pAmount) {
        fAmount = pAmount;
    }

    public Block getSign() {
        return bSign;
    }

    public String getOperation() {
        return sOperation;
    }

    public ItemStack[] getItems() {
        return isItems;
    }

    public boolean isLeftClicking() {
        return bLeftClicking;
    }

    public SSMoneyEventType getTransactionType() {
        return meType;
    }

    public boolean isCheckOnly() {
        return bCheckOnly;
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

package org.wargamer2010.signshop.operations;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.events.IMessagePartContainer;
import org.wargamer2010.signshop.events.SSMoneyEventType;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

public class SignShopArguments implements IMessagePartContainer {
    public SignShopArguments(double pfPrice, ItemStack[] pisItems, List<Block> pContainables, List<Block> pActivatables,
                                SignShopPlayer pssPlayer, SignShopPlayer pssOwner, Block pbSign, String psOperation, BlockFace pbfBlockFace, Action ac, SignShopArgumentsType type) {
        fPrice.setRoot(pfPrice);
        isItems.setRoot(pisItems);
        containables.setRoot(pContainables);
        activatables.setRoot(pActivatables);
        if(pssPlayer != null)
            ssPlayer.setRoot(pssPlayer);
        else
            ssPlayer.setRoot(new SignShopPlayer((Player)null));
        if(pssOwner != null)
            ssOwner.setRoot(pssOwner);
        else
            ssOwner.setRoot(new SignShopPlayer((Player)null));
        bSign.setRoot(pbSign);
        sOperation.setRoot(psOperation);
        bfBlockFace.setRoot(pbfBlockFace);
        aAction.setRoot(ac);
        argumentType = type;
        setDefaultMessageParts();
        fixBooks();
    }

    public SignShopArguments(Seller seller, SignShopPlayer player, SignShopArgumentsType type) {
        if(seller.getSign().getState() instanceof Sign)
            fPrice.setRoot(economyUtil.parsePrice(((Sign)seller.getSign().getState()).getLine(3)));

        isItems.setRoot(seller.getItems());
        containables.setRoot(seller.getContainables());
        activatables.setRoot(seller.getActivatables());
        if(player != null)
            ssPlayer.setRoot(player);
        else
            ssPlayer.setRoot(new SignShopPlayer((Player)null));

        ssOwner.setRoot(seller.getOwner());
        bSign.setRoot(seller.getSign());
        sOperation.setRoot(seller.getOperation());
        bfBlockFace.setRoot(BlockFace.SELF);
        argumentType = type;
        setDefaultMessageParts();
        fixBooks();
    }

    private void fixBooks() {
        if(isItems.getRoot() != null) {
            itemUtil.fixBooks(isItems.getRoot());
        }

        if(containables.getRoot() != null) {
            itemUtil.fixBooks(itemUtil.getAllItemStacksForContainables(containables.getRoot()));
        }

        SignShopPlayer root = ssPlayer.getRoot();
        if(root != null && root.getPlayer() != null) {
            if(root.getItemInHand() != null) {
                ItemStack[] stacks = new ItemStack[1];
                stacks[0] = root.getItemInHand();
                itemUtil.fixBooks(stacks);
            }

            ItemStack[] inventory = root.getInventoryContents();
            itemUtil.fixBooks(inventory);
            root.setInventoryContents(inventory);
        }
    }

    private void setDefaultMessageParts() {
        if(ssPlayer.get() != null) {
            setMessagePart("!customer", ssPlayer.get().getName());
            setMessagePart("!player", ssPlayer.get().getName());
            if(ssPlayer.get().getPlayer() != null && ssPlayer.get().getPlayer().getWorld() != null)
                setMessagePart("!world", ssPlayer.get().getPlayer().getWorld().getName());

            if(Vault.getPermission() != null && ssPlayer.get() != null && ssPlayer.get().getWorld() != null) {
                World world = ssPlayer.get().getWorld();
                String name = ssPlayer.get().getName();
                setMessagePart("!permgroup", Vault.getPermission().getPrimaryGroup(world, name));
            }
        }

        if(fPrice.get() != null)
            setMessagePart("!price", economyUtil.formatMoney(fPrice.get()));

        if(ssOwner.get() != null)
            setMessagePart("!owner", ssOwner.get().getName());

        if(bSign.get() != null) {
            setMessagePart("!x", Integer.toString(bSign.get().getX()));
            setMessagePart("!y", Integer.toString(bSign.get().getY()));
            setMessagePart("!z", Integer.toString(bSign.get().getZ()));

            if(bSign.get().getState() instanceof Sign) {
                String[] sLines = ((Sign) bSign.get().getState()).getLines();
                for(int i = 0; i < sLines.length; i++)
                    setMessagePart(("!line" + (i+1)), (sLines[i] == null ? "" : sLines[i]));
            }
        }

        if(isItems.get() != null && isItems.get().length > 0) {
            setMessagePart("!items", itemUtil.itemStackToString(isItems.get()));
        }
    }

    public void reset() {
        fPrice.setSpecial(false);
        isItems.setSpecial(false);
        containables.setSpecial(false);
        activatables.setSpecial(false);
        ssPlayer.setSpecial(false);
        ssOwner.setSpecial(false);
        bSign.setSpecial(false);
        sOperation.setSpecial(false);
        bfBlockFace.setSpecial(false);
        resetPriceMod();
    }

    public void resetPriceMod() {
        bPriceModApplied = false;
    }

    public static String seperator = "~";

    private SignShopArgument<Double> fPrice = new SignShopArgument<Double>(this);
    public SignShopArgument<Double> getPrice() {
        return fPrice;
    }

    private SignShopArgument<ItemStack[]> isItems = new SignShopArgument<ItemStack[]>(this) {
        @Override
        public void set(ItemStack[] pItems) {
            if(getCollection().forceMessageKeys.containsKey("!items") && argumentType == SignShopArgumentsType.Setup)
                getCollection().miscSettings.put(getCollection().forceMessageKeys.get("!items").replace("!", ""),
                        signshopUtil.implode(itemUtil.convertItemStacksToString(pItems), seperator));
            super.set(pItems);
        }
    };
    public SignShopArgument<ItemStack[]> getItems() {
        return isItems;
    }

    private SignShopArgument<List<Block>> containables = new SignShopArgument<List<Block>>(this);
    public SignShopArgument<List<Block>> getContainables() {
        return containables;
    }

    private SignShopArgument<List<Block>> activatables = new SignShopArgument<List<Block>>(this);
    public SignShopArgument<List<Block>> getActivatables() {
        return activatables;
    }

    private SignShopArgument<SignShopPlayer> ssPlayer = new SignShopArgument<SignShopPlayer>(this);
    public SignShopArgument<SignShopPlayer> getPlayer() {
        return ssPlayer;
    }

    private SignShopArgument<SignShopPlayer> ssOwner = new SignShopArgument<SignShopPlayer>(this);
    public SignShopArgument<SignShopPlayer> getOwner() {
        return ssOwner;
    }

    private SignShopArgument<Block> bSign = new SignShopArgument<Block>(this);
    public SignShopArgument<Block> getSign() {
        return bSign;
    }

    private SignShopArgument<String> sOperation = new SignShopArgument<String>(this);
    public SignShopArgument<String> getOperation() {
        return sOperation;
    }

    private SignShopArgument<String> sEnchantments = new SignShopArgument<String>(this);
    public SignShopArgument<String> getEnchantments() {
        return sEnchantments;
    }

    private SignShopArgument<BlockFace> bfBlockFace = new SignShopArgument<BlockFace>(this);
    public SignShopArgument<BlockFace> getBlockFace() {
        return bfBlockFace;
    }

    private SignShopArgument<Action> aAction = new SignShopArgument<Action>(this);
    public SignShopArgument<Action> getAction() {
        return aAction;
    }

    private List<String> operationParameters = new LinkedList<String>();
    public void setOperationParameters(List<String> pOperationParameters) { operationParameters.clear(); operationParameters.addAll(pOperationParameters); }
    public boolean isOperationParameter(String sOperationParameter) { return operationParameters.contains(sOperationParameter); }
    public boolean hasOperationParameters() { return !operationParameters.isEmpty(); }
    public String getFirstOperationParameter() { return hasOperationParameters() ? operationParameters.get(0) : ""; }

    private SignShopArgumentsType argumentType = SignShopArgumentsType.Unknown;
    public SignShopArgumentsType getArgumentType() { return argumentType; }
    public void setArgumentType(SignShopArgumentsType argumentType) { this.argumentType = argumentType; }

    public Map<String, String> miscSettings = new HashMap<String, String>();
    public Map<String, String> forceMessageKeys = new HashMap<String, String>();
    public boolean bDoNotClearClickmap = false;
    public boolean bPriceModApplied = false;
    public boolean bRunCommandAsUser = false;

    private SSMoneyEventType moneyEventType = SSMoneyEventType.Unknown;
    public SSMoneyEventType getMoneyEventType() {
        return moneyEventType;
    }
    public void setMoneyEventType(SSMoneyEventType type) {
        moneyEventType = type;
    }

    public void ignoreEmptyChest() {
        if(!isOperationParameter("allowemptychest"))
            operationParameters.add("allowemptychest");
    }

    public boolean isLeftClicking() {
        return (getAction().get() == Action.LEFT_CLICK_AIR || getAction().get() == Action.LEFT_CLICK_BLOCK);
    }

    public void sendFailedRequirementsMessage(String messageName) {
        if(!isLeftClicking())
            getPlayer().get().sendMessage(SignShopConfig.getError(messageName, getMessageParts()));
    }

    public boolean isPlayerOnline() {
        return (ssPlayer.get() != null && ssPlayer.get().getPlayer() != null && ssPlayer.get().GetIdentifier() != null);
    }

    public boolean tryToApplyPriceMod() {
        if(bPriceModApplied)
            return false;
        return (bPriceModApplied = true);
    }

    private Map<String, String> messageParts = new LinkedHashMap<String, String>();

    public void setMessagePart(String name, String value) {
        messageParts.put(name, value);
        if(forceMessageKeys.containsKey(name))
            name = forceMessageKeys.get(name);
        messageParts.put(name, value);
    }

    public boolean hasMessagePart(String name) {
        return messageParts.containsKey(name);
    }

    public String getMessagePart(String name) {
        if(hasMessagePart(name))
            return messageParts.get(name);
        return "";
    }

    public Map<String, String> getMessageParts() {
        return Collections.unmodifiableMap(messageParts);
    }

    public Map<String, String> getRawMessageParts() {
        return messageParts;
    }
}

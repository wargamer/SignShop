package org.wargamer2010.signshop.operations;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.events.IMessagePartContainer;
import org.wargamer2010.signshop.events.SSMoneyEventType;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.*;

public class SignShopArguments implements IMessagePartContainer {
    public static String seperator = "~";
    public Map<String, String> miscSettings = new HashMap<>();
    public Map<String, String> forceMessageKeys = new HashMap<>();
    public boolean bDoNotClearClickmap = false;
    public boolean bPriceModApplied = false;
    public boolean bRunCommandAsUser = false;
    private final SignShopArgument<Double> fPrice = new SignShopArgument<>(this);
    private final SignShopArgument<List<Block>> containables = new SignShopArgument<>(this);
    private final SignShopArgument<List<Block>> activatables = new SignShopArgument<>(this);
    private final SignShopArgument<SignShopPlayer> ssPlayer = new SignShopArgument<>(this);
    private final SignShopArgument<SignShopPlayer> ssOwner = new SignShopArgument<>(this);
    private final SignShopArgument<Block> bSign = new SignShopArgument<>(this);
    private final SignShopArgument<String> sOperation = new SignShopArgument<>(this);
    private final SignShopArgument<String> sEnchantments = new SignShopArgument<>(this);
    private final SignShopArgument<BlockFace> bfBlockFace = new SignShopArgument<>(this);
    private final SignShopArgument<Action> aAction = new SignShopArgument<>(this);
    private final List<String> operationParameters = new LinkedList<>();
    private SignShopArgumentsType argumentType;
    private final SignShopArgument<ItemStack[]> isItems = new SignShopArgument<ItemStack[]>(this) {
        @Override
        public void set(ItemStack[] pItems) {
            if (getCollection().forceMessageKeys.containsKey("!items") && argumentType == SignShopArgumentsType.Setup)
                getCollection().miscSettings.put(getCollection().forceMessageKeys.get("!items").replace("!", ""),
                        signshopUtil.implode(itemUtil.convertItemStacksToString(pItems), seperator));
            super.set(pItems);
        }
    };
    private SSMoneyEventType moneyEventType = SSMoneyEventType.Unknown;
    private final Map<String, String> messageParts = new LinkedHashMap<>();

    public SignShopArguments(double pfPrice, ItemStack[] pisItems, List<Block> pContainables, List<Block> pActivatables,
                             SignShopPlayer pssPlayer, SignShopPlayer pssOwner, Block pbSign, String psOperation, BlockFace pbfBlockFace, Action ac, SignShopArgumentsType type) {
        SignShop.debugMessage("Constructing Args!");
        long timeMillis = System.currentTimeMillis();
        fPrice.setRoot(pfPrice);
        isItems.setRoot(pisItems);
        containables.setRoot(pContainables);
        activatables.setRoot(pActivatables);
        if (pssPlayer != null)
            ssPlayer.setRoot(pssPlayer);
        else
            ssPlayer.setRoot(new SignShopPlayer((Player) null));
        if (pssOwner != null)
            ssOwner.setRoot(pssOwner);
        else
            ssOwner.setRoot(new SignShopPlayer((Player) null));
        bSign.setRoot(pbSign);
        sOperation.setRoot(psOperation);
        bfBlockFace.setRoot(pbfBlockFace);
        aAction.setRoot(ac);
        argumentType = type;
        long timeMillis1 = System.currentTimeMillis();
        setDefaultMessageParts();
        long timeMillis2 = System.currentTimeMillis();
        fixBooks();//TODO this make this constructor more expensive
        long timeMillis3 = System.currentTimeMillis();
        SignShop.debugTiming("Args constructor set message parts",timeMillis1,timeMillis2);
        SignShop.debugTiming("Args constructor fixbooks",timeMillis2,timeMillis3);
        SignShop.debugTiming("Args constructor total",timeMillis,timeMillis3);
    }

    public SignShopArguments(Seller seller, SignShopPlayer player, SignShopArgumentsType type) {
        long timeMillis3 = System.currentTimeMillis();
        if (seller.getSign().getState() instanceof Sign)
            fPrice.setRoot(economyUtil.parsePrice(((Sign) seller.getSign().getState()).getLine(3)));

        isItems.setRoot(seller.getItems());
        containables.setRoot(seller.getContainables());
        activatables.setRoot(seller.getActivatables());
        if (player != null)
            ssPlayer.setRoot(player);
        else
            ssPlayer.setRoot(new SignShopPlayer((Player) null));

        ssOwner.setRoot(seller.getOwner());
        bSign.setRoot(seller.getSign());
        sOperation.setRoot(seller.getOperation());
        bfBlockFace.setRoot(BlockFace.SELF);
        argumentType = type;
        setDefaultMessageParts();
        fixBooks();
        long timeMillis4 = System.currentTimeMillis();
        SignShop.debugTiming("Args constructor 2",timeMillis3,timeMillis4);
    }

    private void fixBooks() {//TODO Do we even need to fix books anymore? This adds several millis to each ssArgs creation.
        if (!SignShopConfig.getEnableWrittenBookFix()) return; //Don't do the rest if we aren't even doing this.
        long timeMillis = System.currentTimeMillis();
        if (isItems.getRoot() != null) {
            SignShop.debugMessage("items getRoot is not null");
            itemUtil.fixBooks(isItems.getRoot());
        }
        long timeMillis1 = System.currentTimeMillis();
        if (containables.getRoot() != null) {
            SignShop.debugMessage("containables getRoot is not null");
            itemUtil.fixBooks(itemUtil.getAllItemStacksForContainables(containables.getRoot()));
        }
        long timeMillis2 = System.currentTimeMillis();
        SignShopPlayer ssPlayerRoot = ssPlayer.getRoot();
        if (ssPlayerRoot != null && ssPlayerRoot.getPlayer() != null) {
            SignShop.debugMessage("playerRoot is not null");
            if (ssPlayerRoot.getItemInHand() != null) {
                SignShop.debugMessage("playerRoot itemInHand is not null");
                ItemStack[] stacks = new ItemStack[1];
                stacks[0] = ssPlayerRoot.getItemInHand();
                itemUtil.fixBooks(stacks);
            }

            ItemStack[] inventory = ssPlayerRoot.getInventoryContents();//TODO this already calls fixbooks
            itemUtil.fixBooks(inventory);
            ssPlayerRoot.setInventoryContents(inventory);
        }
        long timeMillis3 = System.currentTimeMillis();
        SignShop.debugTiming("++fixbooks root items",timeMillis,timeMillis1);
        SignShop.debugTiming("++fixbooks root containers",timeMillis1,timeMillis2);
        SignShop.debugTiming("++fixbooks root player",timeMillis2,timeMillis3);
    }

    private void setDefaultMessageParts() {//TODO this is a bit slow
        if (ssPlayer.get() != null) {
            setMessagePart("!customer", ssPlayer.get().getName());
            setMessagePart("!player", ssPlayer.get().getName());
            if (ssPlayer.get().getPlayer() != null && ssPlayer.get().getPlayer().getWorld() != null)
                setMessagePart("!world", ssPlayer.get().getPlayer().getWorld().getName());

            if (Vault.getPermission() != null && ssPlayer.get() != null && ssPlayer.get().getWorld() != null) {
                World world = ssPlayer.get().getWorld();
                String name = ssPlayer.get().getName();
                setMessagePart("!permgroup", Vault.getPermission().getPrimaryGroup(world, name));
            }
        }

        if (fPrice.get() != null)
            setMessagePart("!price", economyUtil.formatMoney(fPrice.get()));

        if (ssOwner.get() != null)
            setMessagePart("!owner", ssOwner.get().getName());

        if (bSign.get() != null) {
            setMessagePart("!x", Integer.toString(bSign.get().getX()));
            setMessagePart("!y", Integer.toString(bSign.get().getY()));
            setMessagePart("!z", Integer.toString(bSign.get().getZ()));

            if (bSign.get().getState() instanceof Sign) {
                String[] sLines = ((Sign) bSign.get().getState()).getLines();
                for (int i = 0; i < sLines.length; i++)
                    setMessagePart(("!line" + (i + 1)), (sLines[i] == null ? "" : sLines[i]));
            }
        }

        if (isItems.get() != null && isItems.get().length > 0) {
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

    public SignShopArgument<Double> getPrice() {
        return fPrice;
    }

    public SignShopArgument<ItemStack[]> getItems() {
        return isItems;
    }

    public SignShopArgument<List<Block>> getContainables() {
        return containables;
    }

    public SignShopArgument<List<Block>> getActivatables() {
        return activatables;
    }

    public SignShopArgument<SignShopPlayer> getPlayer() {
        return ssPlayer;
    }

    public SignShopArgument<SignShopPlayer> getOwner() {
        return ssOwner;
    }

    public SignShopArgument<Block> getSign() {
        return bSign;
    }

    public SignShopArgument<String> getOperation() {
        return sOperation;
    }

    public SignShopArgument<String> getEnchantments() {
        return sEnchantments;
    }

    public SignShopArgument<BlockFace> getBlockFace() {
        return bfBlockFace;
    }

    public SignShopArgument<Action> getAction() {
        return aAction;
    }

    public void setOperationParameters(List<String> pOperationParameters) {
        operationParameters.clear();
        operationParameters.addAll(pOperationParameters);
    }

    public boolean isOperationParameter(String sOperationParameter) {
        return operationParameters.contains(sOperationParameter);
    }

    public boolean hasOperationParameters() {
        return !operationParameters.isEmpty();
    }

    public String getFirstOperationParameter() {
        return hasOperationParameters() ? operationParameters.get(0) : "";
    }

    public SignShopArgumentsType getArgumentType() {
        return argumentType;
    }

    public void setArgumentType(SignShopArgumentsType argumentType) {
        this.argumentType = argumentType;
    }

    public SSMoneyEventType getMoneyEventType() {
        return moneyEventType;
    }

    public void setMoneyEventType(SSMoneyEventType type) {
        moneyEventType = type;
    }

    public void ignoreEmptyChest() {
        if (!isOperationParameter("allowemptychest"))
            operationParameters.add("allowemptychest");
    }

    public boolean isLeftClicking() {
        return (getAction().get() == Action.LEFT_CLICK_AIR || getAction().get() == Action.LEFT_CLICK_BLOCK);
    }

    public void sendFailedRequirementsMessage(String messageName) {
        if (!isLeftClicking())
            getPlayer().get().sendMessage(SignShopConfig.getError(messageName, getMessageParts()));
    }

    public boolean isPlayerOnline() {
        return (ssPlayer.get() != null && ssPlayer.get().getPlayer() != null && ssPlayer.get().GetIdentifier() != null);
    }

    public boolean tryToApplyPriceMod() {
        if (bPriceModApplied)
            return false;
        return (bPriceModApplied = true);
    }

    public void setMessagePart(String name, String value) {
        messageParts.put(name, value);
        if (forceMessageKeys.containsKey(name))
            name = forceMessageKeys.get(name);
        messageParts.put(name, value);
    }

    public boolean hasMessagePart(String name) {
        return messageParts.containsKey(name);
    }

    public String getMessagePart(String name) {
        if (hasMessagePart(name))
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

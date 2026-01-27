package org.wargamer2010.signshop.operations;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.events.IMessagePartContainer;
import org.wargamer2010.signshop.events.SSMoneyEventType;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.*;

/**
 * Mutable context container passed through the SignShop operation pipeline.
 *
 * <p>SignShopArguments is the central data object for all shop operations. It carries
 * transaction state through the three operation phases: {@code setupOperation},
 * {@code checkRequirements}, and {@code runOperation}. Each operation can read and
 * modify values, enabling complex multi-operation workflows.</p>
 *
 * <h2>Operation Pipeline:</h2>
 * <pre>
 * Player clicks shop sign
 *     ↓
 * SignShopArguments created with initial values
 *     ↓
 * For each operation in config:
 *   1. setupOperation(ssArgs) - Shop creation phase
 *   2. checkRequirements(ssArgs, true) - Validation phase
 *   3. runOperation(ssArgs) - Execution phase
 *     ↓
 * Transaction complete
 * </pre>
 *
 * <h2>Argument Types ({@link SignShopArgumentsType}):</h2>
 * <ul>
 *   <li><b>Setup:</b> Creating a new shop (linking sign to chests)</li>
 *   <li><b>Check:</b> Validating transaction requirements (money, stock, permissions)</li>
 *   <li><b>Run:</b> Executing the actual transaction (transfer items, money)</li>
 * </ul>
 *
 * <h2>Root vs Current Values:</h2>
 * <p>Uses {@link SignShopArgument} wrapper with root/current value pattern:
 * <ul>
 *   <li><b>Root:</b> Original value set at creation, preserved across {@link #reset()}</li>
 *   <li><b>Current:</b> Modified value, may change during operation execution</li>
 * </ul>
 * This enables operations to modify values temporarily while preserving originals.</p>
 *
 * <h2>Message Parts:</h2>
 * <p>The {@code messageParts} map stores values for message template substitution.
 * Operations populate this for error messages and transaction confirmations.
 * Example: {@code %price%, %itemname%, %amount%}</p>
 *
 * <h2>Misc Settings:</h2>
 * <p>The {@code miscSettings} map stores shop-specific configuration (e.g., Trade shop
 * items, price multipliers, custom messages). Persisted to sellers.yml.</p>
 *
 * @see SignShopOperation
 * @see SignShopArgumentsType
 * @see SignShopArgument
 * @see IMessagePartContainer
 */
public class SignShopArguments implements IMessagePartContainer {
    public static String separator = "~";
    public Map<String, String> miscSettings = new HashMap<>();
    public Map<String, String> forceMessageKeys = new HashMap<>();
    public boolean bDoNotClearClickmap = false;
    public boolean bPriceModApplied = false;
    public boolean bRunCommandAsUser = false;
    // Reference to seller for accessing cached deserialized items
    private Seller seller = null;
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
    private final SignShopArgument<ItemStack[]> isItems = new SignShopArgument<>(this) {
        @Override
        public void set(ItemStack[] pItems) {
            if (getCollection().forceMessageKeys.containsKey("!items") && argumentType == SignShopArgumentsType.Setup)
                getCollection().miscSettings.put(getCollection().forceMessageKeys.get("!items").replace("!", ""),
                        signshopUtil.implode(itemUtil.convertItemStacksToString(pItems), separator));
            super.set(pItems);
        }
    };
    private SSMoneyEventType moneyEventType = SSMoneyEventType.Unknown;
    private final Map<String, Object> messageParts = new LinkedHashMap<>();

    public SignShopArguments(double pfPrice, ItemStack[] pisItems, List<Block> pContainables, List<Block> pActivatables,
                             SignShopPlayer pssPlayer, SignShopPlayer pssOwner, Block pbSign, String psOperation, BlockFace pbfBlockFace, Action ac, SignShopArgumentsType type) {
        fPrice.setRoot(pfPrice);
        isItems.setRoot(pisItems);
        containables.setRoot(pContainables);
        activatables.setRoot(pActivatables);
        ssPlayer.setRoot(Objects.requireNonNullElseGet(pssPlayer, () -> new SignShopPlayer((Player) null)));
        ssOwner.setRoot(Objects.requireNonNullElseGet(pssOwner, () -> new SignShopPlayer((Player) null)));
        bSign.setRoot(pbSign);
        sOperation.setRoot(psOperation);
        bfBlockFace.setRoot(pbfBlockFace);
        aAction.setRoot(ac);
        argumentType = type;
        setDefaultMessageParts();
    }

    public SignShopArguments(Seller seller, SignShopPlayer player, SignShopArgumentsType type) {
        this.seller = seller;  // Store seller reference for cached item access
        if (seller.getSign().getState() instanceof Sign)
            fPrice.setRoot(economyUtil.parsePrice(((Sign) seller.getSign().getState()).getSide(Side.FRONT).getLine(3)));

        isItems.setRoot(seller.getItems());
        containables.setRoot(seller.getContainables());
        activatables.setRoot(seller.getActivatables());
        ssPlayer.setRoot(Objects.requireNonNullElseGet(player, () -> new SignShopPlayer((Player) null)));

        ssOwner.setRoot(seller.getOwner());
        bSign.setRoot(seller.getSign());
        sOperation.setRoot(seller.getOperation());
        bfBlockFace.setRoot(BlockFace.SELF);
        argumentType = type;
        setDefaultMessageParts();
    }
    

    /**
     * Initializes default message placeholders for shop operations.
     * Called during SignShopArguments construction - executed on every shop interaction.
     */
    private void setDefaultMessageParts() {
        if (ssPlayer.get() != null) {
            setMessagePart("!customer", ssPlayer.get().getName());
            setMessagePart("!player", ssPlayer.get().getName());
            if (ssPlayer.get().getPlayer() != null && ssPlayer.get().getPlayer().getWorld() != null)
                setMessagePart("!world", ssPlayer.get().getPlayer().getWorld().getName());

            if (Vault.getPermission() != null && ssPlayer.get() != null && ssPlayer.get().getWorld() != null) {
                World world = ssPlayer.get().getWorld();
                OfflinePlayer name = ssPlayer.get().getOfflinePlayer();
                setMessagePart("!permgroup", Vault.getPermission().getPrimaryGroup(world.getName(), name));
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
                String[] sLines = ((Sign) bSign.get().getState()).getSide(Side.FRONT).getLines();
                for (int i = 0; i < sLines.length; i++)
                    setMessagePart(("!line" + (i + 1)), (sLines[i] == null ? "" : sLines[i]));
            }
        }

        if (isItems.get() != null && isItems.get().length > 0) {
            setMessagePart("!items", org.wargamer2010.signshop.util.ItemMessagePart.fromItems(isItems.get()));
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
        return hasOperationParameters() ? operationParameters.getFirst() : "";
    }

    public SignShopArgumentsType getArgumentType() {
        return argumentType;
    }

    public void setArgumentType(SignShopArgumentsType argumentType) {
        this.argumentType = argumentType;
    }

    public Seller getSeller() {
        return seller;
    }

    public void setSeller(Seller seller) {
        this.seller = seller;
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
            getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError(messageName, getMessageParts()));
    }

    public boolean isPlayerOnline() {
        return (ssPlayer.get() != null && ssPlayer.get().getPlayer() != null && ssPlayer.get().GetIdentifier() != null);
    }

    public boolean tryToApplyPriceMod() {
        if (bPriceModApplied)
            return false;
        return (bPriceModApplied = true);
    }

    @Override
    public void setMessagePart(String name, Object value) {
        messageParts.put(name, value);
        if (forceMessageKeys.containsKey(name))
            name = forceMessageKeys.get(name);
        messageParts.put(name, value);
    }

    /**
     * Overloaded method for binary compatibility with external plugins.
     * Plugins compiled against older versions expect this signature.
     *
     * @param name The message part key
     * @param value The string value
     */
    public void setMessagePart(String name, String value) {
        setMessagePart(name, (Object) value);
    }

    public boolean hasMessagePart(String name) {
        return messageParts.containsKey(name);
    }

    /**
     * Gets a message part as a string. If the value is an ItemMessagePart,
     * returns its cached string representation.
     *
     * @param name The message part name
     * @return The string value, or empty string if not found
     */
    public String getMessagePart(String name) {
        if (hasMessagePart(name)) {
            Object value = messageParts.get(name);
            if (value instanceof String) {
                return (String) value;
            } else if (value instanceof org.wargamer2010.signshop.util.ItemMessagePart) {
                return ((org.wargamer2010.signshop.util.ItemMessagePart) value).getString();
            }
        }
        return "";
    }

    @Override
    public Map<String, Object> getMessageParts() {
        return Collections.unmodifiableMap(messageParts);
    }

    public Map<String, Object> getRawMessageParts() {
        return messageParts;
    }
}

package org.wargamer2010.signshop.player;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.data.Storage;
import org.wargamer2010.signshop.util.itemUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Wrapper around Bukkit Player providing SignShop-specific functionality.
 *
 * <p>Abstracts player operations for shop owners and customers, handling
 * both online and offline players. Provides methods for inventory management,
 * money operations via Vault, messaging, and permission checks.</p>
 *
 * <p>Instances are cached via {@link PlayerCache} to avoid repeated creation.</p>
 *
 * @see PlayerCache
 * @see PlayerIdentifier
 */
public class SignShopPlayer {

    private String playername = "";
    private PlayerIdentifier playerId = null;
    private final PlayerMetadata meta = new PlayerMetadata(this, SignShop.getInstance());
    private boolean ignoreMessages = false;

    public SignShopPlayer() {
        playername = "";
    }

    public SignShopPlayer(PlayerIdentifier id) {
        if (id == null)
            return;
        playername = id.getName();
        playerId = id;

        if (playername == null)
            playername = "";
    }

    public SignShopPlayer(Player pPlayer) {
        if (pPlayer != null) {
            playerId = new PlayerIdentifier(pPlayer);
            playername = playerId.getName();
        }

        if (playername == null)
            playername = "";
    }

    /**
     * Constructs a SignshopPlayer by player name
     * This is a legacy constructor and is purely here for backwards compatibility
     *
     * @param name Player name
     * @deprecated Lookup by name has been deprecated so use the PlayerIdentifier alternative instead
     */
    @Deprecated
    public SignShopPlayer(String name) {
        SignShopPlayer player = PlayerIdentifier.getByName(name);
        if (player != null) {
            playerId = player.GetIdentifier();
            playername = player.getName();
        }

        if (playername == null)
            playername = "";
    }

    public static void broadcastMsg(World world, String sMessage) {
        for (Player player : Bukkit.getServer().getOnlinePlayers())
            if (player.getWorld() == world)
                player.sendMessage(ChatColor.GOLD + SignShop.getInstance().getSignShopConfig().getChatPrefix() + " [" + world.getName() + "] " + ChatColor.WHITE + sMessage);
    }

    public static boolean isOp(Player player) {
        if (player == null)
            return false;
        SignShopPlayer ssPlayer = PlayerCache.getPlayer(player);
        return ssPlayer.isOp(player.getWorld());
    }

    public void sendMessage(String sMessage) {
        if (sMessage == null || sMessage.trim().isEmpty() || getPlayer() == null || ignoreMessages)
            return;
        if (SignShop.getInstance().getSignShopConfig().getMessageCooldown() <= 0) {
            sendNonDelayedMessage(sMessage);
            return;
        }

        MessageWorker.init();
        MessageWorker.OfferMessage(sMessage, this);
    }


    public void sendNonDelayedMessage(String sMessage) {
        if (sMessage == null || sMessage.trim().isEmpty() || getPlayer() == null || ignoreMessages)
            return;
        String message = (ChatColor.GOLD + SignShop.getInstance().getSignShopConfig().getChatPrefix() + ChatColor.WHITE + " " + sMessage);
        getPlayer().sendMessage(message);
    }

    /**
     * Sends a rich text message with hover events (e.g., item tooltips) to the player.
     * The message respects cooldown settings and will be queued if necessary.
     *
     * @param component The component to send (with hover events)
     */
    public void sendMessage(BaseComponent component) {
        if (component == null || getPlayer() == null || ignoreMessages)
            return;

        // Convert component to plain text for cooldown deduplication
        // Use toPlainText() to strip ALL formatting including color codes
        String plainText = component.toPlainText();
        if (plainText == null || plainText.trim().isEmpty())
            return;

        if (SignShop.getInstance().getSignShopConfig().getMessageCooldown() <= 0) {
            sendNonDelayedMessage(component);
            return;
        }

        MessageWorker.init();
        MessageWorker.OfferMessage(plainText, component, this);
    }

    /**
     * Sends a rich text message immediately without cooldown delay.
     *
     * @param component The component to send
     */
    public void sendNonDelayedMessage(BaseComponent component) {
        if (component == null || getPlayer() == null || ignoreMessages)
            return;

        // Check for empty content - toPlainText() strips ALL formatting including color codes
        String plainText = component.toPlainText();
        if (plainText == null || plainText.trim().isEmpty())
            return;

        // Add chat prefix
        TextComponent prefixedMessage = new TextComponent(
            ChatColor.GOLD + SignShop.getInstance().getSignShopConfig().getChatPrefix() + ChatColor.WHITE + " "
        );
        prefixedMessage.addExtra(component);

        getPlayer().spigot().sendMessage(prefixedMessage);
    }

    public String getName() {
        return playername;
    }

    public Player getPlayer() {
        return playerId == null ? null : playerId.getPlayer();
    }

    public OfflinePlayer getOfflinePlayer() {
        return playerId == null ? null : playerId.getOfflinePlayer();
    }

    public World getWorld() {
        return (getPlayer() == null) ? null : getPlayer().getWorld();
    }

    public boolean compareTo(SignShopPlayer other) {
        if (other == null)
            return false;
        if (GetIdentifier() == null)
            return other.GetIdentifier() == null;
        return other.GetIdentifier().equals(GetIdentifier());
    }

    public void setOp(Boolean OP) {
        if (playername.isEmpty())
            return;
        if (getPlayer() == null) {
            OfflinePlayer player = playerId.getOfflinePlayer();
            if (player != null)
                player.setOp(OP);
        } else {
            getPlayer().setOp(OP);
        }
    }

    public boolean isOp() {
        if (getPlayer() == null)
            return false;
        return isOp(getPlayer().getWorld());
    }

    public boolean isOp(World world) {
        return isOp(world, "");
    }

    public boolean isOp(World world, String perm) {
        if (isOpRaw())
            return true;
        String fullperm = (perm.isEmpty() ? "SignShop.SuperAdmin" : "SignShop.SuperAdmin." + perm);
        return SignShop.usePermissions() && Vault.getPermission().playerHas(world.getName(), getOfflinePlayer(), fullperm.toLowerCase());
    }

    private boolean isOpRaw() {
        if (playername.isEmpty())
            return false;
        if (getPlayer() == null) {
            OfflinePlayer offplayer = playerId.getOfflinePlayer();
            return offplayer != null && offplayer.isOp();
        } else
            return getPlayer().isOp();
    }

    public boolean hasBypassShopPlots(String pluginName) {
        String starPerm = "SignShop.BypassShopPlots.*";
        String perm = starPerm;
        if (!pluginName.isEmpty())
            perm = ("SignShop.BypassShopPlots." + pluginName);
        return (hasPerm(perm, true) || hasPerm(starPerm, true));
    }

    public Boolean hasPerm(String perm, Boolean OPOperation) {
        if (getPlayer() == null)
            return false;
        return hasPerm(perm, getPlayer().getWorld(), OPOperation);
    }

    public boolean hasPerm(String perm, World world, Boolean OPOperation) {
        if (playername == null || playername.isEmpty())
            return true;
        boolean isOP = isOpRaw();
        boolean OPOverride = SignShop.getInstance().getSignShopConfig().getOPOverride();

        // If we're using Permissions, OPOverride is disabled then we need to ignore his OP
        // So let's temporarily disable it so the outcome of the Vault call won't be influenced
        if (SignShop.usePermissions() && isOP && !OPOverride)
            setOp(false);
        // Having Signshop.Superadmin while Permissions are in use should allow you to do everything with SignShop
        // And since the node is explicitly given to a player, the OPOverride setting is not relevant
        if (SignShop.usePermissions() && Vault.getPermission().playerHas(world.getName(), getOfflinePlayer(), "signshop.superadmin")) {
            setOp(isOP);
            return true;
        }

        // If we're using Permissions, OPOverride is enabled and the Player has OP, he can do everything
        if (SignShop.usePermissions() && OPOverride && isOP)
            return true;
            // Using Permissions so check his permissions and restore his OP if he has it
        else if (SignShop.usePermissions() && Vault.getPermission().playerHas(world.getName(), getOfflinePlayer(), perm.toLowerCase())) {
            setOp(isOP);
            return true;
            // Not using Permissions, but he is OP, so he's allowed
        } else if (!SignShop.usePermissions() && isOP)
            return true;
            // Not using Permissions, he doesn't have OP, but it's not an OP Operation
        else if (!SignShop.usePermissions() && !OPOperation)
            return true;
        // Reset OP
        setOp(isOP);
        return false;
    }

    @SuppressWarnings("WrapperTypeMayBePrimitive")
    private boolean isNothing(double amount) {
        Double doubler = amount;
        return (doubler == 0.0f || doubler.isInfinite() || doubler.isNaN());
    }

    public boolean hasNoMoney(double amount) {
        if (isNothing(amount))
            return false;
        if (Vault.getEconomy() == null)
            return true;
        if (playername.isEmpty())
            return false;
        else
            return !Vault.getEconomy().has(getOfflinePlayer(), amount);
    }

    public boolean canNotHaveMoney(double amount) {
        // Negative amounts make no sense in this context, so fix it if needed
        double actual = amount < 0 ? (amount * -1) : amount;

        if (isNothing(actual))
            return false;
        if (Vault.getEconomy() == null)
            return true;
        if (playername.isEmpty())
            return false;
        EconomyResponse response;
        double currentBalance = Vault.getEconomy().getBalance(getOfflinePlayer());

        try {
            response = Vault.getEconomy().depositPlayer(getOfflinePlayer(), actual);
        } catch (java.lang.RuntimeException ex) {
            response = new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "");
            SignShop.getInstance().debugMessage("canNotHaveMoney() caught exception from economy!");
            if (SignShop.getInstance().getSignShopConfig().debugging()) {
                ex.printStackTrace();
            }
        }

        double newBalance = Vault.getEconomy().getBalance(getOfflinePlayer());
        double subtract = (newBalance - currentBalance);

        if (response.type == EconomyResponse.ResponseType.SUCCESS) {
            response = Vault.getEconomy().withdrawPlayer(getOfflinePlayer(), subtract);
            return response.type != EconomyResponse.ResponseType.SUCCESS;
        } else {
            return true;
        }
    }

    public boolean mutateMoney(double amount) {
        if (Vault.getEconomy() == null)
            return false;
        if (playername.isEmpty() || isNothing(amount))
            return true;
        EconomyResponse response;
        try {
            if (amount > 0.0)
                response = Vault.getEconomy().depositPlayer(getOfflinePlayer(), amount);
            else if (amount < 0.0)
                response = Vault.getEconomy().withdrawPlayer(getOfflinePlayer(), Math.abs(amount));
            else
                return true;
        } catch (java.lang.RuntimeException ex) {
            response = new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "");
            SignShop.getInstance().debugMessage("mutateMoney() caught exception from economy!");
            if (SignShop.getInstance().getSignShopConfig().debugging()) {
                ex.printStackTrace();
            }
        }
        return response.type == EconomyResponse.ResponseType.SUCCESS;
    }

    public Map<Integer, ItemStack> givePlayerItems(ItemStack[] isItemsToTake) {
        if (getPlayer() == null)
            return new LinkedHashMap<>();
        ItemStack[] isBackup = itemUtil.getBackupItemStack(isItemsToTake);
        return getPlayer().getInventory().addItem(isBackup);
    }

    public Map<Integer, ItemStack> takePlayerItems(ItemStack[] isItemsToTake) {
        if (getPlayer() == null)
            return new LinkedHashMap<>();
        ItemStack[] isBackup = itemUtil.getBackupItemStack(isItemsToTake);
        return getPlayer().getInventory().removeItem(isBackup);
    }

    public VirtualInventory getVirtualInventory() {
        if (getPlayer() == null)
            return null;
        return new VirtualInventory(getPlayer().getInventory());
    }

    private String[] getPlayerGroups() {
        String[] sGroups = null;
        if (getPlayer() == null)
            return sGroups;
        try {
            sGroups = Vault.getPermission().getPlayerGroups(null, getPlayer());
        } catch (UnsupportedOperationException UnsupportedEX) {
            return sGroups;
        }
        return sGroups;
    }

    public Double getPlayerPricemod(String sOperation, boolean bBuyOperation) {
        Double fPricemod = 1.0d;
        Double fTemp;

        if (Vault.getPermission() == null || getPlayer() == null)
            return fPricemod;
        String[] sGroups = getPlayerGroups();
        if (sGroups == null) return fPricemod;

        boolean firstRun = true;
        for (String sGroup1 : sGroups) {
            String sGroup = sGroup1.toLowerCase();
            if (SignShop.getInstance().getSignShopConfig().getPriceMultipliers().containsKey(sGroup) && SignShop.getInstance().getSignShopConfig().getPriceMultipliers().get(sGroup).containsKey(sOperation)) {
                fTemp = SignShop.getInstance().getSignShopConfig().getPriceMultipliers().get(sGroup).get(sOperation);

                if (bBuyOperation && (fTemp < fPricemod || firstRun))
                    fPricemod = fTemp;
                else if (!bBuyOperation && (fTemp > fPricemod || firstRun))
                    fPricemod = fTemp;
                firstRun = false;
            }
        }
        return fPricemod;
    }

    public int reachedMaxShops() {
        if (Vault.getPermission() == null || playername.isEmpty())
            return 0;
        if (hasPerm("SignShop.ignoremax", true))
            return 0;

        String[] sGroups = getPlayerGroups();
        int iShopAmount = Storage.get().countLocations(this);

        if (SignShop.getInstance().getSignShopConfig().getMaxShopsPerPerson() != 0 && iShopAmount >= SignShop.getInstance().getSignShopConfig().getMaxShopsPerPerson())
            return SignShop.getInstance().getSignShopConfig().getMaxShopsPerPerson();
        if (sGroups == null) return 0;

        int iLimit = 1;
        boolean bInRelGroup = false;
        for (String sGroup1 : sGroups) {
            String sGroup = sGroup1.toLowerCase();
            if (SignShop.getInstance().getSignShopConfig().getShopLimits().containsKey(sGroup)) {
                bInRelGroup = true;
                if (iShopAmount < SignShop.getInstance().getSignShopConfig().getShopLimits().get(sGroup))
                    iLimit = 0;
                else if (iLimit != 0 && SignShop.getInstance().getSignShopConfig().getShopLimits().get(sGroup) > iLimit)
                    iLimit = SignShop.getInstance().getSignShopConfig().getShopLimits().get(sGroup);
            }

        }

        return ((!bInRelGroup) ? 0 : iLimit);
    }

    public ItemStack[] getInventoryContents() {
        if (getPlayer() == null)
            return new ItemStack[0];
        return getPlayer().getInventory().getContents();
    }

    public void setInventoryContents(ItemStack[] newContents) {
        if (getPlayer() == null)
            return;
        getPlayer().getInventory().setContents(newContents);
    }

    public boolean playerExistsOnServer() {
        return (playerId != null && playerId.getOfflinePlayer() != null);
    }

    public PlayerIdentifier GetIdentifier() {
        return playerId;
    }

    public boolean setMeta(String key, String value) {
        return meta.setMetavalue(key, value);
    }

    public String getMeta(String key) {
        return meta.getMetaValue(key);
    }

    public boolean hasMeta(String key) {
        return meta.hasMeta(key);
    }

    public boolean removeMeta(String key) {
        return meta.removeMeta(key);
    }

    public void removeMetaByPrefix(String keyPrefix) {
        meta.removeMetakeyLike(keyPrefix + "%");
    }

    public ItemStack getItemInHand() {
        if (getPlayer() == null)
            return null;
        ItemStack stack = getPlayer().getInventory().getItemInMainHand();
        if (stack.getType() == Material.getMaterial("AIR"))
            return null;
        return stack;
    }

    public boolean isOwner(Seller seller) {
        return seller.getOwner().compareTo(this);
    }

    public boolean isIgnoreMessages() {
        return ignoreMessages;
    }

    public void setIgnoreMessages(boolean ignoreMessages) {
        this.ignoreMessages = ignoreMessages;
    }
}

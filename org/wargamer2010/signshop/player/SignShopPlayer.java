package org.wargamer2010.signshop.player;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.util.itemUtil;

public class SignShopPlayer {
    private Player playerObject = null;
    private String playername = "";
    private PlayerIdentifier playerId = null;
    private PlayerMetadata meta = new PlayerMetadata(this, SignShop.getInstance());

    public SignShopPlayer() {

    }

    public SignShopPlayer(PlayerIdentifier id) {
        if(id == null)
            return;
        playerObject = id.getPlayer();
        playername = id.getName();
        playerId = id;
    }

    public SignShopPlayer(Player pPlayer) {
        playerObject = pPlayer;
        if(playerObject != null) {
            playerId = new PlayerIdentifier(pPlayer);
            playername = playerObject.getName();
        }
    }

    /**
     * Constructs a SignshopPlayer by player name
     * This is a legacy constructor and is purely here for backwards compatibility
     *
     * @param name Player name
     * @deprecated Lookup by name has been deprecated so use the PlayerIdentifier alternative in stead
     */
    @Deprecated
    public SignShopPlayer(String name) {
        SignShopPlayer player = PlayerIdentifier.getByName(name);
        if(player != null) {
            playerObject = player.getPlayer();
            playerId = player.GetIdentifier();
            playername = player.getName();
        }
    }

    public static void broadcastMsg(World world, String sMessage) {
        Player[] players = Bukkit.getServer().getOnlinePlayers();
        for(Player player : players)
            if(player.getWorld() == world)
                player.sendMessage(ChatColor.GOLD+"[SignShop] [" + world.getName() + "] " + ChatColor.WHITE + sMessage);
    }

    public void sendMessage(String sMessage) {
        if(sMessage == null || sMessage.trim().isEmpty() || playerObject == null)
            return;
        if(SignShopConfig.getMessageCooldown() <= 0) {
            sendNonDelayedMessage(sMessage);
            return;
        }

        MessageWorker.init();
        MessageWorker.OfferMessage(sMessage, this);
    }

    public void sendNonDelayedMessage(String sMessage) {
        if(sMessage == null || sMessage.trim().isEmpty() || playerObject == null)
            return;
        String message = (ChatColor.GOLD + "[SignShop] " + ChatColor.WHITE + sMessage);
        playerObject.sendMessage(message);
    }

    public String getName() {
        return playername;
    }

    public Player getPlayer() {
        return playerObject;
    }

    public World getWorld() {
        return (playerObject == null) ? null : playerObject.getWorld();
    }

    public boolean compareTo(SignShopPlayer other) {
        if(other == null)
            return false;
        if(GetIdentifier() == null)
            return other.GetIdentifier() == null;
        return other.GetIdentifier().equals(GetIdentifier());
    }

    public void setOp(Boolean OP) {
        if(playername.isEmpty())
            return;
        if(playerObject == null) {
            OfflinePlayer player = playerId.getOfflinePlayer();
            if(player != null)
                player.setOp(OP);
        } else {
            playerObject.setOp(OP);
        }
    }

    public static boolean isOp(Player player) {
        if(player == null)
            return false;
        SignShopPlayer ssPlayer = new SignShopPlayer(player);
        return ssPlayer.isOp(player.getWorld());
    }

    public boolean isOp() {
        if(playerObject == null)
            return false;
        return isOp(playerObject.getWorld());
    }


    public boolean isOp(World world) {
        return isOp(world, "");
    }

    public boolean isOp(World world, String perm) {
        if(isOpRaw())
            return true;
        String fullperm = (perm.isEmpty() ? "SignShop.SuperAdmin" : "SignShop.SuperAdmin." + perm);
        if(SignShop.usePermissions() && Vault.getPermission().playerHas(world, playername, fullperm.toLowerCase()))
            return true;
        return false;
    }

    private boolean isOpRaw() {
        if(playername.isEmpty())
            return false;
        if(playerObject == null) {
            OfflinePlayer offplayer = playerId.getOfflinePlayer();
            return offplayer != null && offplayer.isOp();
        }
        else
            return playerObject.isOp();
    }

    public boolean hasBypassShopPlots(String pluginName) {
        String starPerm = "SignShop.BypassShopPlots.*";
        String perm = starPerm;
        if(!pluginName.isEmpty())
            perm = ("SignShop.BypassShopPlots." + pluginName);
        return (hasPerm(perm, true) || hasPerm(starPerm, true));
    }

    public Boolean hasPerm(String perm, Boolean OPOperation) {
        if(playerObject == null)
            return false;
        return hasPerm(perm, playerObject.getWorld(), OPOperation);
    }

    public boolean hasPerm(String perm, World world, Boolean OPOperation) {
        if(playername.isEmpty())
            return true;
        Boolean isOP = isOpRaw();
        Boolean OPOverride = SignShopConfig.getOPOverride();
        // If we're using Permissions, OPOverride is disabled then we need to ignore his OP
        // So let's temporarily disable it so the outcome of the Vault call won't be influenced
        if(SignShop.usePermissions() && isOP && !OPOverride)
            setOp(false);
        // If we're using Permissions, OPOverride is enabled and the Player has OP, he can do everything
        if(SignShop.usePermissions() && OPOverride && isOP)
            return true;
        // Using Permissions so check his permissions and restore his OP if he has it
        else if(SignShop.usePermissions() && Vault.getPermission().playerHas(world, playername, perm.toLowerCase())) {
            setOp(isOP);
            return true;
        // Not using Permissions but he is OP, so he's allowed
        } else if(!SignShop.usePermissions() && isOP)
            return true;
        // Not using Permissions, he doesn't have OP but it's not an OP Operation
        else if(!SignShop.usePermissions() && !OPOperation)
            return true;
        // Reset OP
        setOp(isOP);
        return false;
    }

    private boolean isNothing(float amount) {
        Float floater = new Float(amount);
        return (floater == 0.0f || floater.isInfinite() || floater.isNaN());
    }

    public boolean hasMoney(float amount) {
        if(isNothing(amount))
            return true;
        if(Vault.getEconomy() == null)
            return false;
        if(playername.isEmpty())
            return true;
        else
            return Vault.getEconomy().has(playername, amount);
    }

    public boolean canHaveMoney(float amount) {
        // Negative amounts make no sense in this context, so fix it if needed
        float actual = amount < 0 ? (amount * -1) : amount;

        if(isNothing(actual))
            return true;
        if(Vault.getEconomy() == null)
            return false;
        if(playername.isEmpty())
            return true;
        EconomyResponse response;
        double currentBalance = Vault.getEconomy().getBalance(playername);

        try {
            response = Vault.getEconomy().depositPlayer(playername, actual);
        } catch(java.lang.RuntimeException ex) {
            response = new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "");
        }

        double newBalance = Vault.getEconomy().getBalance(playername);
        double subtract = (newBalance - currentBalance);

        if(response.type == EconomyResponse.ResponseType.SUCCESS) {
            response = Vault.getEconomy().withdrawPlayer(playername, subtract);
            return response.type == EconomyResponse.ResponseType.SUCCESS;
        }
        else {
            return false;
        }
    }

    public boolean mutateMoney(float amount) {
        if(Vault.getEconomy() == null)
            return false;
        if(playername.isEmpty() || isNothing(amount))
            return true;
        EconomyResponse response;
        try {
            if(amount > 0.0)
                response = Vault.getEconomy().depositPlayer(playername, amount);
            else if(amount < 0.0)
                response = Vault.getEconomy().withdrawPlayer(playername, Math.abs(amount));
            else
                return true;
        } catch(java.lang.RuntimeException ex) {
            response = new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "");
        }

        if(response.type == EconomyResponse.ResponseType.SUCCESS)
            return true;
        else
            return false;
    }

    public void givePlayerItems(ItemStack[] isItemsToTake) {
        if(playerObject == null)
            return;
        ItemStack[] isBackup = itemUtil.getBackupItemStack(isItemsToTake);
        playerObject.getInventory().addItem(isBackup);
    }

    public void takePlayerItems(ItemStack[] isItemsToTake) {
        if(playerObject == null)
            return;
        ItemStack[] isBackup = itemUtil.getBackupItemStack(isItemsToTake);
        playerObject.getInventory().removeItem(isBackup);
    }

    private String[] getPlayerGroups() {
        String[] sGroups = null;
        if(playerObject == null)
            return sGroups;
        try {
            sGroups = Vault.getPermission().getPlayerGroups(playerObject);
        } catch(UnsupportedOperationException UnsupportedEX) {
            return sGroups;
        }
        return sGroups;
    }

    public Float getPlayerPricemod(String sOperation) {
        Float fPricemod = 1.0f;
        Float fTemp;
        boolean bBuyorSell = true;
        boolean first = true;

        if(Vault.getPermission() == null || playerObject == null)
            return fPricemod;
        String[] sGroups = getPlayerGroups();
        if(sGroups == null) return fPricemod;

        if(sGroups.length == 0)
            return fPricemod;
        for(int i = 0; i < sGroups.length; i++) {
            String sGroup = sGroups[i].toLowerCase();
            if(SignShopConfig.getPriceMultipliers().containsKey(sGroup) && SignShopConfig.getPriceMultipliers().get(sGroup).containsKey(sOperation)) {
                fTemp = SignShopConfig.getPriceMultipliers().get(sGroup).get(sOperation);
                if(first && fTemp != 1.0f) {
                    // Use the first price multiplier to check whether it's a buy or sell transaction
                    // TODO: This setting should probably be pulled from somewhere else but will work with a proper configuration for now
                    first = false;
                    bBuyorSell = (fTemp < 1.0f);
                }

                if(bBuyorSell && fTemp < fPricemod)
                    fPricemod = fTemp;
                else if(!bBuyorSell && fTemp > fPricemod)
                    fPricemod = fTemp;
            }
        }
        return fPricemod;
    }

    public int reachedMaxShops() {
        if(Vault.getPermission() == null || playername.isEmpty())
            return 0;
        if(hasPerm("SignShop.ignoremax", true))
            return 0;

        String[] sGroups = getPlayerGroups();
        int iShopAmount = Storage.get().countLocations(this);

        if(SignShopConfig.getMaxShopsPerPerson() != 0 && iShopAmount >= SignShopConfig.getMaxShopsPerPerson()) return SignShopConfig.getMaxShopsPerPerson();
        if(sGroups == null) return 0;

        int iLimit = 1;
        Boolean bInRelGroup = false;
        for(int i = 0; i < sGroups.length; i++) {
            String sGroup = sGroups[i].toLowerCase();
            if(SignShopConfig.getShopLimits().containsKey(sGroup)) {
                bInRelGroup = true;
                if(iShopAmount < SignShopConfig.getShopLimits().get(sGroup))
                    iLimit = 0;
                else if(iLimit != 0 && SignShopConfig.getShopLimits().get(sGroup) > iLimit)
                    iLimit = SignShopConfig.getShopLimits().get(sGroup);
            }

        }

        return ((!bInRelGroup) ? 0 : iLimit);
    }

    public ItemStack[] getInventoryContents() {
        if(playerObject == null)
            return new ItemStack[0];
        return playerObject.getInventory().getContents();
    }

    public void setInventoryContents(ItemStack[] newContents) {
        if(playerObject == null)
            return;
        playerObject.getInventory().setContents(newContents);
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

    public boolean removeMetaByPrefix(String keyPrefix) {
        return meta.removeMetakeyLike(keyPrefix + "%");
    }

    public ItemStack getItemInHand() {
        if(playerObject == null)
            return null;
        ItemStack stack = playerObject.getItemInHand();
        if(stack.getType() == Material.getMaterial("AIR"))
            return null;
        return stack;
    }

    public boolean isOwner(Seller seller) {
        return seller.getOwner().compareTo(this);
    }
}

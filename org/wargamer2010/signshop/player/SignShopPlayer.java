package org.wargamer2010.signshop.player;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
    private Player ssPlayer = null;
    private String sPlayername = "";
    private PlayerMetadata meta = new PlayerMetadata(this, SignShop.getInstance());

    public SignShopPlayer() {

    }

    public SignShopPlayer(String sName) {
        Player[] players = Bukkit.getServer().getOnlinePlayers();

        for(Player pPlayer : players) {
            if(pPlayer.getName().equals(sName)){
                ssPlayer = pPlayer;
            }
        }
        sPlayername = sName;
    }

    public SignShopPlayer(Player pPlayer) {
        ssPlayer = pPlayer;
        if(ssPlayer != null)
            sPlayername = ssPlayer.getName();
    }

    public static void broadcastMsg(World world, String sMessage) {
        Player[] players = Bukkit.getServer().getOnlinePlayers();
        for(Player player : players)
            if(player.getWorld() == world)
                player.sendMessage(ChatColor.GOLD+"[SignShop] [" + world.getName() + "] " + ChatColor.WHITE + sMessage);
    }

    public void sendMessage(String sMessage) {
        if(sMessage == null || sMessage.trim().isEmpty() || ssPlayer == null)
            return;
        if(SignShopConfig.getMessageCooldown() <= 0) {
            sendNonDelayedMessage(sMessage);
            return;
        }

        MessageWorker.init();
        MessageWorker.OfferMessage(sMessage, this);
    }

    public void sendNonDelayedMessage(String sMessage) {
        if(sMessage == null || sMessage.trim().isEmpty() || ssPlayer == null)
            return;
        String message = (ChatColor.GOLD + "[SignShop] " + ChatColor.WHITE + sMessage);
        ssPlayer.sendMessage(message);
    }

    public String getName() {
        return sPlayername;
    }

    public Player getPlayer() {
        return ssPlayer;
    }

    public World getWorld() {
        return (ssPlayer == null) ? null : ssPlayer.getWorld();
    }

    public void setOp(Boolean OP) {
        if(sPlayername.isEmpty())
            return;
        if(ssPlayer == null)
            Bukkit.getOfflinePlayer(sPlayername).setOp(OP);
        else
            ssPlayer.setOp(OP);
    }

    public static boolean isOp(Player player) {
        if(player == null)
            return false;
        SignShopPlayer ssPlayer = new SignShopPlayer(player);
        return ssPlayer.isOp(player.getWorld());
    }

    public boolean isOp() {
        if(ssPlayer == null)
            return false;
        return isOp(ssPlayer.getWorld());
    }


    public boolean isOp(World world) {
        return isOp(world, "");
    }

    public boolean isOp(World world, String perm) {
        if(isOpRaw())
            return true;
        String fullperm = (perm.isEmpty() ? "SignShop.SuperAdmin" : "SignShop.SuperAdmin." + perm);
        if(SignShop.usePermissions() && Vault.permission.playerHas(world, sPlayername, fullperm.toLowerCase()))
            return true;
        return false;
    }

    private boolean isOpRaw() {
        if(sPlayername.isEmpty())
            return false;
        if(ssPlayer == null)
            return Bukkit.getOfflinePlayer(sPlayername).isOp();
        else
            return ssPlayer.isOp();
    }

    public Boolean hasPerm(String perm, Boolean OPOperation) {
        if(ssPlayer == null)
            return false;
        return hasPerm(perm, ssPlayer.getWorld(), OPOperation);
    }

    public boolean hasPerm(String perm, World world, Boolean OPOperation) {
        if(Vault.permission == null)
            return false;
        if(sPlayername.isEmpty())
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
        else if(SignShop.usePermissions() && Vault.permission.playerHas(world, sPlayername, perm.toLowerCase())) {
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
        if(Vault.economy == null)
            return false;
        if(sPlayername.isEmpty())
            return true;
        else
            return Vault.economy.has(sPlayername, amount);
    }

    public boolean canHaveMoney(float amount) {
        if(isNothing(amount))
            return true;
        if(Vault.economy == null)
            return false;
        if(sPlayername.isEmpty())
            return true;
        EconomyResponse response;
        try {
            response = Vault.economy.depositPlayer(sPlayername, amount);
        } catch(java.lang.RuntimeException ex) {
            response = new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "");
        }

        if(response.type == EconomyResponse.ResponseType.SUCCESS)
            Vault.economy.withdrawPlayer(sPlayername, Math.abs(amount));
        else
            return false;
        return true;
    }

    public boolean mutateMoney(float amount) {
        if(Vault.economy == null)
            return false;
        if(sPlayername.isEmpty() || isNothing(amount))
            return true;
        EconomyResponse response;
        try {
            if(amount > 0.0)
                response = Vault.economy.depositPlayer(sPlayername, amount);
            else if(amount < 0.0)
                response = Vault.economy.withdrawPlayer(sPlayername, Math.abs(amount));
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
        if(ssPlayer == null)
            return;
        ItemStack[] isBackup = itemUtil.getBackupItemStack(isItemsToTake);
        ssPlayer.getInventory().addItem(isBackup);
    }

    public void takePlayerItems(ItemStack[] isItemsToTake) {
        if(ssPlayer == null)
            return;
        ItemStack[] isBackup = itemUtil.getBackupItemStack(isItemsToTake);
        ssPlayer.getInventory().removeItem(isBackup);
    }

    private String[] getPlayerGroups() {
        String[] sGroups = null;
        if(ssPlayer == null)
            return sGroups;
        try {
            sGroups = Vault.permission.getPlayerGroups(ssPlayer);
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

        if(Vault.permission == null || ssPlayer == null)
            return fPricemod;
        String[] sGroups = getPlayerGroups();
        if(sGroups == null) return fPricemod;

        if(sGroups.length == 0)
            return fPricemod;
        for(int i = 0; i < sGroups.length; i++) {
            String sGroup = sGroups[i].toLowerCase();
            if(SignShopConfig.PriceMultipliers.containsKey(sGroup) && SignShopConfig.PriceMultipliers.get(sGroup).containsKey(sOperation)) {
                fTemp = SignShopConfig.PriceMultipliers.get(sGroup).get(sOperation);
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
        if(Vault.permission == null || sPlayername.isEmpty())
            return 0;
        if(hasPerm("SignShop.ignoremax", true))
            return 0;

        String[] sGroups = getPlayerGroups();
        int iShopAmount = Storage.get().countLocations(sPlayername);

        if(SignShopConfig.getMaxShopsPerPerson() != 0 && iShopAmount >= SignShopConfig.getMaxShopsPerPerson()) return SignShopConfig.getMaxShopsPerPerson();
        if(sGroups == null) return 0;

        int iLimit = 1;
        Boolean bInRelGroup = false;
        for(int i = 0; i < sGroups.length; i++) {
            String sGroup = sGroups[i].toLowerCase();
            if(SignShopConfig.ShopLimits.containsKey(sGroup)) {
                bInRelGroup = true;
                if(iShopAmount < SignShopConfig.ShopLimits.get(sGroup))
                    iLimit = 0;
                else if(iLimit != 0 && SignShopConfig.ShopLimits.get(sGroup) > iLimit)
                    iLimit = SignShopConfig.ShopLimits.get(sGroup);
            }

        }

        return ((!bInRelGroup) ? 0 : iLimit);
    }

    public ItemStack[] getInventoryContents() {
        if(ssPlayer == null)
            return null;
        return ssPlayer.getInventory().getContents();
    }

    public void setInventoryContents(ItemStack[] newContents) {
        if(ssPlayer == null)
            return;
        ssPlayer.getInventory().setContents(newContents);
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

    public ItemStack getItemInHand() {
        if(ssPlayer == null)
            return null;
        ItemStack stack = ssPlayer.getItemInHand();
        if(stack.getType() == Material.getMaterial("AIR"))
            return null;
        return stack;
    }

    public boolean isOwner(Seller seller) {
        return (seller.getOwner().equals(sPlayername));
    }

    private class MessageCount {
        private int iCount = 1;
        private long lLastSeen;

        private MessageCount(int pCount, long pLastSeen) {
            iCount = pCount;
            lLastSeen = pLastSeen;
        }

        public int getCount() {
            return iCount;
        }

        public void incCount() {
            this.iCount++;
        }

        public void clrCount() {
            this.iCount = 0;
        }

        public long getLastSeen() {
            return lLastSeen;
        }

        public void setLastSeen(long lLastSeen) {
            this.lLastSeen = lLastSeen;
        }
    }

}

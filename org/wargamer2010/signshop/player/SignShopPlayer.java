package org.wargamer2010.signshop.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.itemUtil;

import net.milkbowl.vault.economy.EconomyResponse;

public class SignShopPlayer {
    Player ssPlayer = null;
    String sPlayername = "";
    
    public SignShopPlayer() {        
    }
    
    public SignShopPlayer(String sName) {
        Player[] players = Bukkit.getServer().getOnlinePlayers();

        for(Player pPlayer : players){
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
        if(sMessage == null || sMessage.trim().equals("") || ssPlayer == null)
            return;
        ssPlayer.sendMessage(ChatColor.GOLD + "[SignShop] " + ChatColor.WHITE + sMessage);
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
        if(sPlayername.equals(""))
            return;
        if(ssPlayer == null)            
            Bukkit.getOfflinePlayer(sPlayername).setOp(OP);
        else
            ssPlayer.setOp(OP);
    }
    
    public Boolean isOp() {
        if(sPlayername.equals(""))
            return false;
        if(ssPlayer == null)            
            return Bukkit.getOfflinePlayer(sPlayername).isOp();
        else
            return ssPlayer.isOp();
    }
    
    public Boolean hasPerm(String perm, Boolean OPOperation) { 
        if(ssPlayer == null)
            return false;
        return this.hasPerm(perm, ssPlayer.getWorld(), OPOperation);
    }

    public Boolean hasPerm(String perm, World world, Boolean OPOperation) {
        if(Vault.permission == null)
            return false;
        if(sPlayername.equals(""))
            return true;
        Boolean isOP = isOp();
        Boolean OPOverride = SignShop.getOPOverride();
        // If we're using Permissions, OPOverride is disabled then we need to ignore his OP
        // So let's temporarily disable it so the outcome of the Vault call won't be influenced
        if(SignShop.USE_PERMISSIONS && isOP && !OPOverride)
            setOp(false);
        // If we're using Permissions, OPOverride is enabled and the Player has OP, he can do everything
        if(SignShop.USE_PERMISSIONS && OPOverride && isOP)
            return true;
        // Using Permissions so check his permissions and restore his OP if he has it
        else if(SignShop.USE_PERMISSIONS && Vault.permission.playerHas(world, sPlayername, perm)) {
            setOp(isOP);
            return true;
        // Not using Permissions but he is OP, so he's allowed
        } else if(!SignShop.USE_PERMISSIONS && isOP)
            return true;
        // Not using Permissions, he doesn't have OP but it's not an OP Operation
        else if(!SignShop.USE_PERMISSIONS && !OPOperation)
            return true;
        // Reset OP
        setOp(isOP);
        return false;
    }
    
    public Boolean hasMoney(float amount) {        
        if(Vault.economy == null)
            return false;
        if(sPlayername.equals(""))
            return true;
        else
            return Vault.economy.has(sPlayername, amount);
    }
    
    public Boolean mutateMoney(float amount) {
        if(Vault.economy == null)
            return false;
        if(sPlayername.equals(""))
            return true;
        EconomyResponse response;
        if(amount > 0.0)
            response = Vault.economy.depositPlayer(sPlayername, amount);
        else if(amount < 0.0)
            response = Vault.economy.withdrawPlayer(sPlayername, Math.abs(amount));
        else
            return true;
        if(response.type == EconomyResponse.ResponseType.SUCCESS)
            return true;
        else
            return false;
    }
    
    public void givePlayerItems(ItemStack[] isItemsToTake) {
        if(ssPlayer == null)
            return;
        ItemStack[] isBackup = new ItemStack[isItemsToTake.length];        
        for(int i = 0; i < isItemsToTake.length; i++){
            if(isItemsToTake[i] != null){
                isBackup[i] = new ItemStack(
                    isItemsToTake[i].getType(),
                    isItemsToTake[i].getAmount(),
                    isItemsToTake[i].getDurability()
                );
                itemUtil.addSafeEnchantments(isBackup[i], isItemsToTake[i].getEnchantments());                
                if(isItemsToTake[i].getData() != null){
                    isBackup[i].setData(isItemsToTake[i].getData());
                }
            }
        }
        ssPlayer.getInventory().addItem(isBackup);
    }
    
    public void takePlayerItems(ItemStack[] isItemsToTake) {
        if(ssPlayer == null)
            return;
        ItemStack[] isBackup = new ItemStack[isItemsToTake.length];        
        for(int i = 0; i < isItemsToTake.length; i++){
            if(isItemsToTake[i] != null){
                isBackup[i] = new ItemStack(
                    isItemsToTake[i].getType(),
                    isItemsToTake[i].getAmount(),
                    isItemsToTake[i].getDurability()
                );
                itemUtil.addSafeEnchantments(isBackup[i], isItemsToTake[i].getEnchantments());                
                if(isItemsToTake[i].getData() != null){
                    isBackup[i].setData(isItemsToTake[i].getData());
                }
            }
        }        
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
    
    public Float getPlayerPricemod(String sOperation, Boolean bBuyorSell) {
        Float fPricemod = 1.0f;
        Float fTemp = fPricemod;
        if(Vault.permission == null || ssPlayer == null)
            return fPricemod;
        String[] sGroups = getPlayerGroups();
        if(sGroups == null) return fPricemod;
                
        if(sGroups.length == 0)
            return fPricemod;
        for(int i = 0; i < sGroups.length; i++) {
            String sGroup = sGroups[i].toLowerCase();
            if(SignShop.PriceMultipliers.containsKey(sGroup) && SignShop.PriceMultipliers.get(sGroup).containsKey(sOperation)) {
                fTemp = SignShop.PriceMultipliers.get(sGroup).get(sOperation);
                if(bBuyorSell && fTemp < fPricemod)
                    fPricemod = fTemp;
                else if(!bBuyorSell && fTemp > fPricemod)
                    fPricemod = fTemp;
            }
        }       
        return fPricemod;
    }
    
    public int reachedMaxShops() {        
        if(Vault.permission == null || sPlayername.equals(""))
            return 0;
        if(hasPerm("SignShop.ignoremax", true))
            return 0;
        
        String[] sGroups = getPlayerGroups();        
        int iShopAmount = SignShop.Storage.countLocations(sPlayername);
        
        if(SignShop.getMaxShopsPerPerson() != 0 && iShopAmount >= SignShop.getMaxShopsPerPerson()) return SignShop.getMaxShopsPerPerson();        
        if(sGroups == null) return 0;
        
        int iLimit = 1;
        Boolean bInRelGroup = false;
        for(int i = 0; i < sGroups.length; i++) {
            String sGroup = sGroups[i].toLowerCase();
            if(SignShop.ShopLimits.containsKey(sGroup)) {
                bInRelGroup = true;
                if(iShopAmount < SignShop.ShopLimits.get(sGroup))
                    iLimit = 0;
                else if(iLimit != 0 && SignShop.ShopLimits.get(sGroup) > iLimit)
                    iLimit = SignShop.ShopLimits.get(sGroup);
            }
            
        }
        
        return ((!bInRelGroup) ? 0 : iLimit);
    }
    
}

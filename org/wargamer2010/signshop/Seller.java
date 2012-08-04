package org.wargamer2010.signshop;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.wargamer2010.signshop.util.itemUtil;
import com.miykeal.showCaseStandalone.*;
import org.wargamer2010.signshop.util.signshopUtil;

public class Seller {        
    private List<Block> containables = new LinkedList();
    private List<Block> activatables = new LinkedList();
    private ItemStack[] isItems;
    private Map<String, String> miscProps = new HashMap<String, String>();
    
    private String owner;
    private String world;
    
    public Seller(String sPlayer, String sWorld, List<Block> pContainables, List<Block> pActivatables, ItemStack[] isChestItems, Map<String, String> pMiscProps) {
        owner = sPlayer;
        world = sWorld;

        isItems = itemUtil.getBackupItemStack(isChestItems);
        containables = pContainables;
        activatables = pActivatables;
        if(pMiscProps != null)
            miscProps.putAll(pMiscProps);
    }
    
    public Seller(String sPlayer, String sWorld, List<Block> pContainables, List<Block> pActivatables, ItemStack[] isChestItems){
        this(sPlayer, sWorld, pContainables, pActivatables, isChestItems, null);
    }
    
    public ItemStack[] getItems() {        
        return itemUtil.getBackupItemStack(isItems);
    }
    
    public List<Block> getContainables() {
        return containables;
    }
    
    public List<Block> getActivatables() {
        return activatables;
    }
        
    public String getOwner() {
        return owner;
    }
    
    public void setOwner(String newowner) {
        owner = newowner;
    }
    
    public String getWorld() {
        return world;
    }
    
    public Map<String, String> getMisc() {
        return miscProps;
    }
    
    public void cleanUp() {
        if(miscProps.containsKey("showcaselocation")) {
            if(Bukkit.getServer().getPluginManager().getPlugin("ShowCaseStandalone") == null)
                return;
            Location loc = signshopUtil.convertStringToLocation(miscProps.get("showcaselocation"), Bukkit.getWorld(world));
            ShowCaseStandalone scs = (ShowCaseStandalone) Bukkit.getServer().getPluginManager().getPlugin("ShowCaseStandalone");
            com.miykeal.showCaseStandalone.Shops.Shop shop = null;
            try {
                shop = scs.getShopHandler().getShopForBlock(Bukkit.getWorld(world).getBlockAt(loc));
            } catch(Exception ex) {
                return;
            }
            if(shop != null)
                scs.getShopHandler().removeShop(shop);
        }
    }
}

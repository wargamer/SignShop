package org.wargamer2010.signshop;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.wargamer2010.signshop.util.itemUtil;

public class Seller {        
    private List<Block> containables = new ArrayList();
    private List<Block> activatables = new ArrayList();
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
    
    public String getWorld() {
        return world;
    }
    
    public Map<String, String> getMisc() {
        return miscProps;
    }
}

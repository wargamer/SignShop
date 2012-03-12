package org.wargamer2010.signshop;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import java.util.Map;
import java.util.ArrayList;

public class Seller{
    public String world;
    public int x;
    public int y;
    public int z;

    public Integer[] items;
    public Integer[] amounts;
    public Byte[] datas;
    public Short[] durabilities;
    public ArrayList<Map<Enchantment, Integer>> enchantments;

    public String owner;

    public Seller(String sPlayer,Block bChest,ItemStack[] isChestItems){
        this.owner = sPlayer;

        this.world = bChest.getWorld().getName();
        this.x = bChest.getLocation().getBlockX();
        this.y = bChest.getLocation().getBlockY();
        this.z = bChest.getLocation().getBlockZ();

        this.items = new Integer[isChestItems.length];
        this.amounts = new Integer[isChestItems.length];
        this.durabilities = new Short[isChestItems.length];
        this.datas = new Byte[isChestItems.length];
        this.enchantments = new ArrayList<Map<Enchantment, Integer>>();
        
        for(int i=0;i<isChestItems.length;i++){
            if(isChestItems[i] != null && isChestItems[i].getAmount() > 0){
                this.items[i] = isChestItems[i].getTypeId();
                this.amounts[i] = isChestItems[i].getAmount();
                this.durabilities[i] = isChestItems[i].getDurability();
                this.enchantments.add(isChestItems[i].getEnchantments());
                
                if(isChestItems[i].getData() != null){
                    this.datas[i] = isChestItems[i].getData().getData();
                }
            }
        }
    }

    public ItemStack[] getItems(){
        ItemStack[] isItems = new ItemStack[items.length];

        for(int i=0;i<items.length;i++){
            isItems[i] = new ItemStack(items[i],amounts[i]);
            if(datas[i] != null){
                isItems[i].getData().setData(datas[i]);
            }
            if(durabilities[i] != null){
                isItems[i].setDurability(durabilities[i]);
            }
            if(enchantments.get(i) != null){
                isItems[i].addEnchantments(enchantments.get(i));
            }
        }

        return isItems;
    }

    public Block getChest(){
        return Bukkit.getServer().getWorld(this.world).getBlockAt(this.x,this.y,this.z);
    }
}

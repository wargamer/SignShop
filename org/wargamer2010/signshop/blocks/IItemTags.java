
package org.wargamer2010.signshop.blocks;

import org.bukkit.Material;

public interface IItemTags {
    public org.bukkit.inventory.ItemStack copyTags(org.bukkit.inventory.ItemStack from, org.bukkit.inventory.ItemStack to);

    public void setItemMaxSize(Material material, int maxstacksize);

    public org.bukkit.inventory.ItemStack[] getCraftItemstacks(int size, Material mat, int amount, short damage);

    public org.bukkit.inventory.ItemStack getCraftItemstack(Material mat, Integer amount, Short damage);

    public org.bukkit.inventory.ItemStack[] getCraftItemstacks(int size, org.bukkit.inventory.ItemStack stack);

    public org.bukkit.inventory.ItemStack getCraftItemstack(org.bukkit.inventory.ItemStack stack);
}

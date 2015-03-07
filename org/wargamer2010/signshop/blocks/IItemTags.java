
package org.wargamer2010.signshop.blocks;

import org.bukkit.Material;

public interface IItemTags {
    public org.bukkit.inventory.ItemStack copyTags(org.bukkit.inventory.ItemStack from, org.bukkit.inventory.ItemStack to);

    public org.bukkit.inventory.ItemStack getCraftItemstack(Material mat, Integer amount, Short damage);
}

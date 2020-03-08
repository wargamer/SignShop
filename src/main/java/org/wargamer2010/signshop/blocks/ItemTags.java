package org.wargamer2010.signshop.blocks;


import org.bukkit.Material;

public class ItemTags implements IItemTags {
    @Override
    public org.bukkit.inventory.ItemStack copyTags(org.bukkit.inventory.ItemStack from, org.bukkit.inventory.ItemStack to) {
        if(from == null || to == null || from.getItemMeta() == null)
            return to;
        to.setItemMeta(from.getItemMeta().clone());
        return to;
    }

    @Override
    public org.bukkit.inventory.ItemStack getCraftItemstack(Material mat, Integer amount, Short damage) {
        return new org.bukkit.inventory.ItemStack(mat, amount, damage);
    }
}

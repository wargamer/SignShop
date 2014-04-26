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
    public org.bukkit.inventory.ItemStack[] getCraftItemstacks(int size, Material mat, int amount, short damage) {
        return getCraftItemstacks(size, new org.bukkit.inventory.ItemStack(mat, amount, damage));
    }

    @Override
    public org.bukkit.inventory.ItemStack getCraftItemstack(Material mat, Integer amount, Short damage) {
        return getCraftItemstack(new org.bukkit.inventory.ItemStack(mat, amount, damage));
    }

    @Override
    public org.bukkit.inventory.ItemStack[] getCraftItemstacks(int size, org.bukkit.inventory.ItemStack stack) {
        org.bukkit.inventory.ItemStack[] stacks = new org.bukkit.inventory.ItemStack[size];
        for(int i = 0; i < size; i++) {
            Object temp = getCraftItemstack(stack);
            if(temp != null)
                stacks[i] = (org.bukkit.inventory.ItemStack)temp;
        }
        return stacks;
    }

    @Override
    public org.bukkit.inventory.ItemStack getCraftItemstack(org.bukkit.inventory.ItemStack stack) {
        return stack;
    }
}

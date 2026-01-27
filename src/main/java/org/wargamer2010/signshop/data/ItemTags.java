package org.wargamer2010.signshop.data;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Implementation for copying item metadata between ItemStacks.
 */
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
        ItemStack itemStack = new ItemStack(mat, amount);
        ItemMeta itemMeta = itemStack.getItemMeta();
        ((Damageable) itemMeta).setDamage(damage);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}

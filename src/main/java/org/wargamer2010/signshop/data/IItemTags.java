
package org.wargamer2010.signshop.data;

import org.bukkit.Material;

/**
 * Interface for item tag copying operations.
 */
public interface IItemTags {
    org.bukkit.inventory.ItemStack copyTags(org.bukkit.inventory.ItemStack from, org.bukkit.inventory.ItemStack to);

    org.bukkit.inventory.ItemStack getCraftItemstack(Material mat, Integer amount, Short damage);
}

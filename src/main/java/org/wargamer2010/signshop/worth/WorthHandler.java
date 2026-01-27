package org.wargamer2010.signshop.worth;

import org.bukkit.inventory.ItemStack;

/**
 * Interface for integrating with economy plugins that provide item worth values.
 * Implementations query external plugins like Essentials or CMI for item prices.
 */
public interface WorthHandler {
    double getPrice(ItemStack stack);
}


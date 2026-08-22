package org.wargamer2010.signshop.util;

import org.bukkit.inventory.ItemStack;

/**
 * Wrapper for ItemStack arrays in message parts that provides lazy string conversion
 * and caching for efficient message processing with hover tooltips.
 * This class allows the same items to be converted to both strings (for cooldown keys
 * and command placeholders) and components (for rich chat messages with hover events)
 * without duplicate computation.
 */
public class ItemMessagePart {
    private final ItemStack[] items;
    private String cachedString = null;

    private ItemMessagePart(ItemStack[] items) {
        this.items = items != null ? items : new ItemStack[0];
    }

    /**
     * Creates an ItemMessagePart from an array of items.
     *
     * @param items The items to wrap
     * @return A new ItemMessagePart instance
     */
    public static ItemMessagePart fromItems(ItemStack[] items) {
        return new ItemMessagePart(items);
    }

    /**
     * Gets the string representation of the items, using cached value if available.
     * This is used for cooldown message deduplication and command placeholder replacement.
     *
     * @return String representation of items (e.g., "5 Diamond Sword, 3 Iron Pickaxe")
     */
    public String getString() {
        if (cachedString == null) {
            cachedString = itemUtil.itemStackToString(items);
        }
        return cachedString;
    }

    /**
     * Gets the raw ItemStack array for component building with hover events.
     *
     * @return The wrapped ItemStack array
     */
    public ItemStack[] getItems() {
        return items;
    }
}

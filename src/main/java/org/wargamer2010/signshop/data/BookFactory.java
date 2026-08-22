
package org.wargamer2010.signshop.data;

import org.bukkit.inventory.meta.BookMeta;

/**
 * Factory for creating IBookItem instances from ItemStacks.
 */
public class BookFactory {
    private BookFactory() {

    }

    private static IItemTags tags;
    
    /**
     * For unit testing purposes
     * @param newTags Tags to set
     */
    public static void setItemTags(IItemTags newTags) {
        tags = newTags;
    }

    public static IBookItem getBookItem(org.bukkit.inventory.ItemStack stack) {

        try {
            // Bukkit API 1.9.4+
            BookMeta.Generation.class.isEnum();
            return new BookItem(stack);
        } catch( NoClassDefFoundError e ) {
            // Bukkit API 1.8.8-
            return new LegacyBookItem(stack);
        }

    }

    public static IItemTags getItemTags() {
        return tags == null ? new ItemTags() : tags;
    }
}

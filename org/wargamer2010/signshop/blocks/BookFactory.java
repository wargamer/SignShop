
package org.wargamer2010.signshop.blocks;

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
        return new BookItem(stack);
    }

    public static IItemTags getItemTags() {
        return tags == null ? new ItemTags() : tags;
    }
}

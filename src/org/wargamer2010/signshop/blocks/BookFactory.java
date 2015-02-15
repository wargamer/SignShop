
package org.wargamer2010.signshop.blocks;

public class BookFactory {
    private BookFactory() {

    }

    public static IBookItem getBookItem(org.bukkit.inventory.ItemStack stack) {
        return new BookItem(stack);
    }

    public static IItemTags getItemTags() {
        return new ItemTags();
    }
}

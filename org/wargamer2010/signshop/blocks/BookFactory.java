
package org.wargamer2010.signshop.blocks;

import java.util.logging.Level;
import org.wargamer2010.signshop.SignShop;

public class BookFactory {
    private static String namespaceType = "";
    
    private BookFactory() {

    }

    public static IBookItem getBookItem(org.bukkit.inventory.ItemStack stack) {
        String useType = getNamespaceType();
        IBookItem item;
        if(useType.equals("pre145"))
            item = new org.wargamer2010.signshop.blocks.BookItem(stack);
        else
            item = new org.wargamer2010.signshop.blocks.v145.BookItem(stack);
        return item;
    }

    public static IItemTags getItemTags() {
        String useType = getNamespaceType();
        IItemTags tags;
        if(useType.equals("pre145"))
            tags = new org.wargamer2010.signshop.blocks.itemTags();
        else
            tags = new org.wargamer2010.signshop.blocks.v145.itemTags();

        return tags;
    }

    protected static String getNamespaceType() {
        if(namespaceType.isEmpty()) {
            if(tryReflection("org.bukkit.craftbukkit.inventory.CraftItemStack"))
                namespaceType = "pre145";
            else
                namespaceType = "post145";
        }
        return namespaceType;
    }

    protected static boolean tryReflection(String fullClassname) {
        try {
            Class.forName(fullClassname);
            return true;
        } catch (ClassNotFoundException ex) { }

        return false;
    }
}

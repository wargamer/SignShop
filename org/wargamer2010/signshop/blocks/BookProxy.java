
package org.wargamer2010.signshop.blocks;

public class BookProxy {
    private static String bookType = "";
    private static String tagsType = "";

    private BookProxy() {

    }

    public static IBookItem getBookItem(org.bukkit.inventory.ItemStack stack) {
        String useType;
        if(bookType.isEmpty()) {
            if(tryReflection("org.bukkit.craftbukkit.inventory.CraftItemStack", 1) != null)
                useType = "org.wargamer2010.signshop.blocks.BookItem";
            else if(tryReflection("org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack", 1) != null)
                useType = "org.wargamer2010.signshop.blocks.v145.BookItem";
            else
                useType = "";
            bookType = useType;
        } else {
            useType = bookType;
        }

        Object tryme = tryReflection(useType);
        if(tryme != null) {
            IBookItem item = (IBookItem) tryme;
            item.setStack(stack);
            return item;
        }
        return null;
    }

    public static IItemTags getItemTags() {
        String useType;
        if(tagsType.isEmpty()) {
            if(tryReflection("org.bukkit.craftbukkit.inventory.CraftItemStack", 1) != null)
                useType = "org.wargamer2010.signshop.blocks.itemTags";
            else if(tryReflection("org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack", 1) != null)
                useType = "org.wargamer2010.signshop.blocks.v145.itemTags";
            else
                useType = "";
            tagsType = useType;
        } else {
            useType = tagsType;
        }
        Object tryme = tryReflection(useType);
        if(tryme != null)
            return (IItemTags) tryme;
        return null;
    }

    protected static Object tryReflection(String fullClassname) {
        try {
            Class<?> fc = (Class<?>)Class.forName(fullClassname);
            return fc.newInstance();
        } catch (Exception ex) {
            // Way too many exceptions could be thrown by the statements above
            // So for the sake of my sanity, we'll just catch everything
            return null;
        }
    }

    protected static Object tryReflection(String fullClassname, Integer number) {
        try {
            Class<?> fc = (Class<?>)Class.forName(fullClassname);
            return fc.getConstructor(int.class).newInstance(number);
        } catch (Exception ex) {
            // Way too many exceptions could be thrown by the statements above
            // So for the sake of my sanity, we'll just catch everything
            return null;
        }
    }
}

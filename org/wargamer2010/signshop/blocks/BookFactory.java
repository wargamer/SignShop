
package org.wargamer2010.signshop.blocks;

public class BookFactory {
    private static String namespaceType = "";

    private BookFactory() {

    }

    public static IBookItem getBookItem(org.bukkit.inventory.ItemStack stack) {
        String useType = getNamespaceType();
        IBookItem item;
        if(useType.equals("pre145"))
            item = new org.wargamer2010.signshop.blocks.BookItem(stack);
        else if(useType.equals("v145"))
            item = new org.wargamer2010.signshop.blocks.v145.BookItem(stack);
        else
            return null;
        return item;
    }

    public static IItemTags getItemTags() {
        String useType = getNamespaceType();
        IItemTags tags;
        if(useType.equals("pre145"))
            tags = new org.wargamer2010.signshop.blocks.itemTags();
        else if(useType.equals("v145"))
            tags = new org.wargamer2010.signshop.blocks.v145.itemTags();
        else
            return null;
        return tags;
    }

    protected static String getNamespaceType() {
        if(namespaceType.isEmpty()) {
            if(tryReflection("org.bukkit.craftbukkit.inventory.CraftItemStack", 1) != null)
                namespaceType = "pre145";
            else if(tryReflection("org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack", 1) != null)
                namespaceType = "v145";
            else
                namespaceType = "unkown";
        }
        return namespaceType;
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

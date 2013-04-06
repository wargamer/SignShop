
package org.wargamer2010.signshop.blocks;

import org.wargamer2010.signshop.util.SSBukkitVersion;
import org.wargamer2010.signshop.util.versionUtil;

public class BookFactory {
    private BookFactory() {

    }

    public static IBookItem getBookItem(org.bukkit.inventory.ItemStack stack) {
        IBookItem item;
        if(versionUtil.getBukkitVersionType() == SSBukkitVersion.Pre145)
            item = new org.wargamer2010.signshop.blocks.BookItem(stack);
        else
            item = new org.wargamer2010.signshop.blocks.v145.BookItem(stack);
        return item;
    }

    public static IItemTags getItemTags() {
        IItemTags tags;
        if(versionUtil.getBukkitVersionType() == SSBukkitVersion.Pre145)
            tags = new org.wargamer2010.signshop.blocks.itemTags();
        else
            tags = new org.wargamer2010.signshop.blocks.v145.itemTags();

        return tags;
    }
}

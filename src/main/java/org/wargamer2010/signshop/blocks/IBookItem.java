
package org.wargamer2010.signshop.blocks;

import org.bukkit.inventory.ItemStack;

public interface IBookItem {
    String[] getPages();

    String getAuthor();

    String getTitle();

    Integer getGeneration();

    void setPages(String[] newpages);

    void addPages(String[] newpages);

    void setAuthor(String author);

    void setTitle(String title);

    void setGeneration(Integer generation);

    void copyFrom(IBookItem item);

    ItemStack getStack();
}

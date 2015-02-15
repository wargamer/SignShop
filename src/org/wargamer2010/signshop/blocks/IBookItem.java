
package org.wargamer2010.signshop.blocks;

import org.bukkit.inventory.ItemStack;

public interface IBookItem {
    public String[] getPages();

    public String getAuthor();

    public String getTitle();

    public void setPages(String[] newpages);

    public void addPages(String[] newpages);

    public void setAuthor(String author);

    public void setTitle(String title);

    public ItemStack getStack();
}

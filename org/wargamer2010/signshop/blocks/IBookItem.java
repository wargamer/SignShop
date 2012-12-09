
package org.wargamer2010.signshop.blocks;

public interface IBookItem {
    public void setStack(org.bukkit.inventory.ItemStack item);

    public String[] getPages();

    public String getAuthor();

    public String getTitle();

    public void setPages(String[] newpages);

    public void addPages(String[] newpages);

    public void setAuthor(String author);

    public void setTitle(String title);

    public org.bukkit.inventory.ItemStack getItemStack();
}

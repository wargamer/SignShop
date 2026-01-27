package org.wargamer2010.signshop.data;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.wargamer2010.signshop.SignShop;

import java.util.Arrays;
import java.util.logging.Level;

/**
 * Wrapper for written book metadata (pages, author, title, generation).
 */
public class BookItem implements IBookItem {

        protected BookMeta meta = null;
    protected ItemStack _stack;

        public BookItem(org.bukkit.inventory.ItemStack pItem) {
            if(pItem.getItemMeta() instanceof BookMeta) {
                meta = (BookMeta)pItem.getItemMeta();
                pItem.setItemMeta(meta);
            }
            this._stack = pItem;
        }

        @Override
        public String[] getPages() {
            if(meta == null || !meta.hasPages())
                return new String[1];
            String[] arr = new String[meta.getPages().size()];
            return meta.getPages().toArray(arr);
        }

        @Override
        public String getAuthor() {
            if(meta == null || !meta.hasAuthor())
                return "";
            return meta.getAuthor();
        }

        @Override
        public String getTitle() {
            if(meta == null || !meta.hasTitle())
                return "";
            return meta.getTitle();
        }

        @Override
        public Integer getGeneration() {
            // Some 1.9.4 servers are missing .hasGeneration()
            if(meta == null || meta.getGeneration() == null)
                return 0;
            return meta.getGeneration().ordinal();
        }

        @Override
        public void setPages(String[] newpages) {
            if(meta == null)
                return;
            meta.setPages(Arrays.asList(newpages));
            updateMeta();
        }

        @Override
        public void addPages(String[] newpages) {
            if(meta == null)
                return;
            meta.addPage(newpages);
            updateMeta();
        }

        @Override
        public void setAuthor(String author) {
            if(meta == null)
                return;
            meta.setAuthor(author);
            updateMeta();
        }

        @Override
        public void setTitle(String title) {
            if(meta == null)
                return;
            meta.setTitle(title);
            updateMeta();
        }

        @Override
        public void setGeneration(Integer generation) {
            if(meta == null)
                return;
            else if(generation == null)
            {
                meta.setGeneration(null);
                return;
            }

            try {
                Generation enumGen = Generation.values()[generation];
                meta.setGeneration(enumGen);
            } catch(ArrayIndexOutOfBoundsException e) {
                SignShop.log("Book's generation is out of bounds; leaving as default (ORIGINAL)", Level.WARNING);
            }
        }

        @Override
        public void copyFrom(IBookItem item) {
            if(meta == null)
                return;
            setTitle(item.getTitle());
            setAuthor(item.getAuthor());
            setPages(item.getPages());
            setGeneration(item.getGeneration());

            updateMeta();
        }

        @Override
        public ItemStack getStack() {
            return _stack;
        }

        protected void updateMeta() {
            _stack.setItemMeta(meta);
        }
}
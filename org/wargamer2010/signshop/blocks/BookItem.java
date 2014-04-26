/*
 * Copyright (C) 2012  Joshua Reetz

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package org.wargamer2010.signshop.blocks;


import java.util.Arrays;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookItem implements IBookItem {

        private BookMeta meta = null;
        private ItemStack _stack = null;

        public BookItem(org.bukkit.inventory.ItemStack pItem) {
            if(pItem.getItemMeta() instanceof BookMeta) {
                meta = (BookMeta)pItem.getItemMeta();
                pItem.setItemMeta(meta);
            }
            this._stack = pItem;
        }

        @Override
        public String[] getPages() {
            if(meta == null)
                return new String[1];
            String[] arr = new String[meta.getPages().size()];
            return meta.getPages().toArray(arr);
        }

        @Override
        public String getAuthor() {
            if(meta == null)
                return "";
            return meta.getAuthor();
        }

        @Override
        public String getTitle() {
            if(meta == null)
                return "";
            return meta.getTitle();
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
        public ItemStack getStack() {
            return _stack;
        }

        private void updateMeta() {
            _stack.setItemMeta(meta);
        }
}
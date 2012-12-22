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


import net.minecraft.server.v1_4_6.NBTTagCompound;
import net.minecraft.server.v1_4_6.NBTTagList;
import net.minecraft.server.v1_4_6.NBTTagString;

import org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class BookItem implements IBookItem {

        private net.minecraft.server.v1_4_6.ItemStack item = null;
        private CraftItemStack stack = null;

        public BookItem(org.bukkit.inventory.ItemStack item) {
            if(item instanceof CraftItemStack) {
                    stack = (CraftItemStack)item;
                    this.item = CraftItemStack.asNMSCopy(stack);
            } else if(item instanceof org.bukkit.inventory.ItemStack) {
                    stack = CraftItemStack.asCraftCopy(item);
                    this.item = CraftItemStack.asNMSCopy(item);
            }
        }

        @Override
        public String[] getPages() {
            NBTTagCompound tags = item.getTag();
            if(tags == null) {
                    return null;
            }
            NBTTagList pages = tags.getList("pages");
            String[] pagestrings = new String[pages.size()];
            for(int i = 0; i < pages.size(); i++) {
                    pagestrings[i] = pages.get(i).toString();
            }
            return pagestrings;
        }

        @Override
        public String getAuthor() {
            NBTTagCompound tags = item.getTag();
            if(tags == null) {
                    return null;
            }
            String author = tags.getString("author");
            return author;
        }

        @Override
        public String getTitle() {
            NBTTagCompound tags = item.getTag();
            if(tags == null) {
                    return null;
            }
            String title = tags.getString("title");
            return title;
        }

        @Override
        public void setPages(String[] newpages) {
            NBTTagCompound tags = item.tag;
            if (tags == null) {
                tags = item.tag = new NBTTagCompound();
            }
            NBTTagList pages = new NBTTagList("pages");
            //we don't want to throw any errors if the book is blank!
            if(newpages.length == 0) {
                    pages.add(new NBTTagString("1", ""));
            }else {
                    for(int i = 0; i < newpages.length; i++) {
                            pages.add(new NBTTagString("" + i + "", newpages[i]));
                    }
            }
            tags.set("pages", pages);
        }

        @Override
        public void addPages(String[] newpages) {
            NBTTagCompound tags = item.tag;
            if (tags == null) {
                tags = item.tag = new NBTTagCompound();
            }
            NBTTagList pages;
            if(getPages() == null) {
                    pages = new NBTTagList("pages");
            }else {
                    pages = tags.getList("pages");
            }
            //we don't want to throw any errors if the book is blank!
            if(newpages.length == 0 && pages.size() == 0) {
                    pages.add(new NBTTagString("1", ""));
            }else {
                    for(int i = 0; i < newpages.length; i++) {
                            pages.add(new NBTTagString("" + pages.size() + "", newpages[i]));
                    }
            }
            tags.set("pages", pages);
        }

        @Override
        public void setAuthor(String author) {
            NBTTagCompound tags = item.tag;
            if (tags == null) {
                tags = item.tag = new NBTTagCompound();
            }
            if(author != null && !author.isEmpty()) {
                    tags.setString("author", author);
            }
        }

        @Override
        public void setTitle(String title) {
            NBTTagCompound tags = item.tag;
            if (tags == null) {
                tags = item.tag = new NBTTagCompound();
            }
            if(title != null && !title.isEmpty()) {
                    tags.setString("title", title);
            }
        }

        @Override
        public ItemStack getStack() {
            return stack;
        }
}
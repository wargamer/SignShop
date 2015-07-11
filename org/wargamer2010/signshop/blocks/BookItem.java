package org.wargamer2010.signshop.blocks;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.logging.Level;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.wargamer2010.signshop.SignShop;

public class BookItem implements IBookItem {

        private BookMeta meta = null;
        private ItemStack _stack = null;

        private static boolean attemptedReflection = false;
        private static Field reflectedGenerationField = null;

        private static final String generationError = "Failed to set Generation for Written Book, API might have changed. Please report this problem.";

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
        public Integer getGeneration() {
            Field field = getGenerationField();
            if(field == null)
                return null;
            try {
                return (Integer)field.get(meta);
            } catch (IllegalArgumentException ex) {
                SignShop.log(generationError, Level.WARNING);
            } catch (IllegalAccessException ex) {
                SignShop.log(generationError, Level.WARNING);
            }

            return null;
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
            Field field = getGenerationField();
            if(field == null || generation == null)
                return;
            try {
                field.set(meta, generation);
                updateMeta();
            } catch (IllegalArgumentException ex) {
                SignShop.log(generationError, Level.WARNING);
            } catch (IllegalAccessException ex) {
                SignShop.log(generationError, Level.WARNING);
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

        private void updateMeta() {
            _stack.setItemMeta(meta);
        }

        private Field getGenerationField() {
            if(meta == null)
                return null;
            if(attemptedReflection)
                return reflectedGenerationField;

            try {
                reflectedGenerationField = meta.getClass().getSuperclass().getDeclaredField("generation");
                reflectedGenerationField.setAccessible(true);
            }
            catch (NoSuchFieldException ex) { reflectedGenerationField = null; }
            catch (SecurityException ex) { reflectedGenerationField = null; }
            catch (IllegalArgumentException ex) { reflectedGenerationField = null; }

            attemptedReflection = true;

            if(reflectedGenerationField == null) {
                SignShop.log(generationError, Level.WARNING);
            }

            return reflectedGenerationField;
        }
}
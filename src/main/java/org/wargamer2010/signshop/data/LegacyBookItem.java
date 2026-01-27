package org.wargamer2010.signshop.data;

import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.SignShop;

import java.lang.reflect.Field;
import java.util.logging.Level;

/** Adds backwards compatibility for Generation API missing before Bukkit 1.8.8 */
public class LegacyBookItem extends BookItem {

        private static boolean attemptedReflection = false;
        private static Field reflectedGenerationField = null;

        private static final String generationError = "Failed to set Generation for Written Book. Please report this problem.";

    LegacyBookItem(ItemStack pItem) {
            super(pItem);
        }

        @Override
        public Integer getGeneration() {
            Field field = getGenerationField();
            if(field == null)
                return null;
            try {
                return (Integer)field.get(meta);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                SignShop.log(generationError, Level.WARNING);
            }

            return null;
        }

        @Override
        public void setGeneration(Integer generation) {
            Field field = getGenerationField();
            if(field == null || generation == null)
                return;
            try {
                field.set(meta, generation);
                updateMeta();
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                SignShop.log(generationError, Level.WARNING);
            }
        }

        private Field getGenerationField() {
            if(meta == null)
                return null;
            if(attemptedReflection)
                return reflectedGenerationField;

            try {
                reflectedGenerationField = meta.getClass().getSuperclass().getDeclaredField("generation");
                reflectedGenerationField.setAccessible(true);
            } catch (NoSuchFieldException | IllegalArgumentException | SecurityException ex) {
                reflectedGenerationField = null;
            }

            attemptedReflection = true;

            if(reflectedGenerationField == null) {
                SignShop.log(generationError, Level.WARNING);
            }

            return reflectedGenerationField;
        }
}
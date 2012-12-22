package org.wargamer2010.signshop.blocks;


import com.bergerkiller.bukkit.common.SafeField;
import java.util.logging.Level;
import org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack;
import net.minecraft.server.v1_4_6.ItemStack;
import net.minecraft.server.v1_4_6.NBTTagCompound;
import org.bukkit.Material;
import org.wargamer2010.signshop.SignShop;

public class itemTags implements IItemTags {
    @Override
    public org.bukkit.inventory.ItemStack copyTags(org.bukkit.inventory.ItemStack from, org.bukkit.inventory.ItemStack to) {
        try {
            ItemStack s = CraftItemStack.asNMSCopy(from);
            if(s.getTag() == null) {
                s.setTag(new NBTTagCompound());
            }
            CraftItemStack.asNMSCopy(to).setTag((NBTTagCompound)s.getTag().clone());
            return to;
        } catch(ClassCastException ex) {
            return to;
        }

    }

    @Override
    public void setItemMaxSize(Material material, int maxstacksize) {
        SafeField.set(net.minecraft.server.Item.byId[material.getId()], "maxStackSize", maxstacksize);
    }

    @Override
    public org.bukkit.inventory.ItemStack[] getCraftItemstacks(int size, Material mat, int amount, short damage) {
        return getCraftItemstacks(size, new org.bukkit.inventory.ItemStack(mat, amount, damage));
    }

    @Override
    public org.bukkit.inventory.ItemStack getCraftItemstack(Material mat, Integer amount, Short damage) {
        return getCraftItemstack(new org.bukkit.inventory.ItemStack(mat, amount, damage));
    }

    @Override
    public org.bukkit.inventory.ItemStack[] getCraftItemstacks(int size, org.bukkit.inventory.ItemStack stack) {
        org.bukkit.inventory.ItemStack[] stacks = new org.bukkit.inventory.ItemStack[size];
        for(int i = 0; i < size; i++) {
            Object temp = getCraftItemstack(stack);
            if(temp != null)
                stacks[i] = (org.bukkit.inventory.ItemStack)temp;
        }
        return stacks;
    }

    @Override
    public org.bukkit.inventory.ItemStack getCraftItemstack(org.bukkit.inventory.ItemStack stack) {
        return new org.bukkit.craftbukkit.inventory.CraftItemStack(stack);
    }
}

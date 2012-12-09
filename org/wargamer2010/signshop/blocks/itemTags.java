package org.wargamer2010.signshop.blocks;


import com.bergerkiller.bukkit.common.SafeField;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import net.minecraft.server.ItemStack;
import net.minecraft.server.NBTTagCompound;
import org.bukkit.Material;

public class itemTags implements IItemTags {
    @Override
    public org.bukkit.inventory.ItemStack copyTags(org.bukkit.inventory.ItemStack from, org.bukkit.inventory.ItemStack to) {
        try {
            ItemStack s = ((CraftItemStack) from).getHandle();
            if(s.tag == null) {
                s.tag = new NBTTagCompound();
            }
            ((CraftItemStack) to).getHandle().setTag((NBTTagCompound)s.tag.clone());
            return to;
        } catch(ClassCastException ex) {
            return to;
        }

    }

    @Override
    public void setItemMaxSize(Material material, int maxstacksize) {
        SafeField.set(net.minecraft.server.Item.byId[material.getId()], "maxStackSize", maxstacksize);
    }
}

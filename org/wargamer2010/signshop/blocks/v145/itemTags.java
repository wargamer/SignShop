package org.wargamer2010.signshop.blocks.v145;


import com.bergerkiller.bukkit.common.SafeField;
import net.minecraft.server.v1_4_5.ItemStack;
import net.minecraft.server.v1_4_5.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack;
import org.wargamer2010.signshop.blocks.IItemTags;

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
        SafeField.set(net.minecraft.server.v1_4_5.Item.byId[material.getId()], "maxStackSize", maxstacksize);
    }
}

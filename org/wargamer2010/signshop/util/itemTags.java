package org.wargamer2010.signshop.util;


import org.bukkit.craftbukkit.inventory.CraftItemStack;
import net.minecraft.server.ItemStack;
import net.minecraft.server.NBTTagCompound;

public class itemTags {
    public static org.bukkit.inventory.ItemStack copyTags(org.bukkit.inventory.ItemStack from, org.bukkit.inventory.ItemStack to) {
        ItemStack s = ((CraftItemStack) from).getHandle();        
        if(s.tag == null) {
            s.tag = new NBTTagCompound();
        }
        ((CraftItemStack) to).getHandle().setTag((NBTTagCompound)s.tag.clone());        
        return to;
    }
}

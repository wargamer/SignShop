
package org.wargamer2010.signshop.blocks;

import net.drgnome.nbtlib.NBT;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class NBTUtil {
    private NBTUtil() {

    }

    public static String getNBTAsString(ItemStack from) {
        if(!isNBTLibLoaded())
            return "";
        try {
            return (NBT.saveItemStack64(from));
        } catch(Exception ex) {
            return "";
        }
    }

    public static ItemStack getStackFromNBT(String nbt) {
        if(!isNBTLibLoaded())
            return null;
        if(nbt == null || nbt.isEmpty())
            return null;
        try {
            return NBT.loadItemStack64(nbt);
        } catch(Exception ex) {
            return null;
        }
    }

    private static boolean isNBTLibLoaded() {
        Plugin plg = Bukkit.getPluginManager().getPlugin("NBTLib");
        if(plg == null)
            return false;
        return plg.isEnabled();
    }
}

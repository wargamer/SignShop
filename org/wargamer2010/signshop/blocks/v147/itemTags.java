package org.wargamer2010.signshop.blocks.v147;


import com.bergerkiller.bukkit.common.SafeField;
import org.bukkit.Material;

public class itemTags extends org.wargamer2010.signshop.blocks.v145.itemTags {
    @Override
    public void setItemMaxSize(Material material, int maxstacksize) {
        SafeField.set(net.minecraft.server.v1_4_R1.Item.byId[material.getId()], "maxStackSize", maxstacksize);
    }
}

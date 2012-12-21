package org.wargamer2010.signshop.blocks.v146;


import com.bergerkiller.bukkit.common.SafeField;
import org.bukkit.Material;

public class itemTags extends org.wargamer2010.signshop.blocks.v145.itemTags {
    @Override
    public void setItemMaxSize(Material material, int maxstacksize) {
        SafeField.set(net.minecraft.server.v1_4_6.Item.byId[material.getId()], "maxStackSize", maxstacksize);
    }
}

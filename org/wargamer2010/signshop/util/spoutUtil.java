package org.wargamer2010.signshop.util;

import org.getspout.spoutapi.material.Material;
import org.getspout.spoutapi.material.MaterialData;


public class spoutUtil {
    private spoutUtil() {

    }

    public static String getName(org.bukkit.material.MaterialData data, Short durability) {
        if(data == null)
            return null;
        Material spoutmat = MaterialData.getMaterial(data.getItemTypeId(), durability);
        if(spoutmat != null)
            return spoutmat.getName();
        else
            return null;
    }
}

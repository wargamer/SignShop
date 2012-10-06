package org.wargamer2010.signshop.util;

import org.getspout.spoutapi.material.MaterialData;


public class spoutUtil {
    public static String getName(org.bukkit.material.MaterialData data, Short durability) {                     
        return MaterialData.getMaterial(data.getItemTypeId(), durability).getName();        
    }
}

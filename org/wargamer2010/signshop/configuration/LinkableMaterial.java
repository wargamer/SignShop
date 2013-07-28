
package org.wargamer2010.signshop.configuration;

import org.bukkit.Material;

public class LinkableMaterial {
    private Material materialName = Material.AIR;
    private String alias = "";
    private byte data = -1; // -1 serves as a wildcard

    public LinkableMaterial(Material materialName, String alias) {
        this(materialName, alias, (byte)-1);
    }

    public LinkableMaterial(Material materialName, String alias, byte data) {
        this.materialName = materialName;
        this.data = data;
        this.alias = alias;
    }

    public Material getMaterialName() {
        return materialName;
    }

    public String getAlias() {
        return alias;
    }

    public short getData() {
        return data;
    }
}

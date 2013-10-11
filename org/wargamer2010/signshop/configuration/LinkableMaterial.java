
package org.wargamer2010.signshop.configuration;

import org.bukkit.Material;

public class LinkableMaterial {
    private String materialName = Material.AIR.toString();
    private String alias = "";
    private byte data = -1; // -1 serves as a wildcard

    public LinkableMaterial(Material materialName, String alias) {
        this(materialName, alias, (byte)-1);
    }

    public LinkableMaterial(Material materialName, String alias, byte data) {
        this(materialName.toString(), alias, data);
    }

    public LinkableMaterial(String materialName, String alias, byte data) {
        this.materialName = materialName;
        this.data = data;
        this.alias = alias;
    }

    public String getMaterialName() {
        return materialName;
    }

    public String getAlias() {
        return alias;
    }

    public short getData() {
        return data;
    }
}

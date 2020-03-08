
package org.wargamer2010.signshop.configuration;

import org.bukkit.Material;

public class LinkableMaterial {
    private String materialName;
    private String alias;


    public LinkableMaterial(Material materialName, String alias) {
        this(materialName.toString(), alias);
    }

    public LinkableMaterial(String materialName, String alias) {
        this.materialName = materialName;
        this.alias = alias;
    }

    public String getMaterialName() {
        return materialName;
    }

    public String getAlias() {
        return alias;
    }


}

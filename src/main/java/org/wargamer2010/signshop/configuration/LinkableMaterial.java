
package org.wargamer2010.signshop.configuration;

import org.bukkit.Material;

/**
 * Represents a material that can be linked to SignShop shops.
 *
 * <p>Defines materials (chests, levers, etc.) that players can select when
 * creating shops, along with optional aliases for display purposes.</p>
 */
public class LinkableMaterial {
    private final String materialName;
    private final String alias;


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

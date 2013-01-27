package org.wargamer2010.signshop.blocks.v147;


import java.util.logging.Level;
import org.bukkit.Material;
import org.wargamer2010.signshop.SignShop;

public class itemTags extends org.wargamer2010.signshop.blocks.v145.itemTags {
    @Override
    public void setItemMaxSize(Material material, int maxstacksize) {
        // Support for this has ended due to Bukkit shifting package names around too often
        SignShop.log("Support for sign stacking has ended with the release of 1.4.7. For information, please visit http://tiny.cc/signshop", Level.WARNING);
    }
}

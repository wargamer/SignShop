package org.wargamer2010.signshop.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class ResidenceHook implements Hook {

    @Override
    public String getName() {
        return "Residence";
    }

    @Override
    public Boolean canBuild(Player player, Block block) {
        if(HookManager.getHook("Residence") == null)
            return true;
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(block.getLocation());
        if(res != null) {
            if(!res.getPermissions().playerHas(player.getName(), "container", false) && !Residence.isResAdminOn(player)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Boolean protectBlock(Player player, Block block) {
        return false;
    }
}

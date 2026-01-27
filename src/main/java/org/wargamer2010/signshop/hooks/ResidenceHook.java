package org.wargamer2010.signshop.hooks;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Residence integration hook that checks container permissions in claimed residences.
 */
public class ResidenceHook implements Hook {

    @Override
    public String getName() {
        return "Residence";
    }

    @Override
    public Boolean canBuild(Player player, Block block) {
        if (HookManager.getHook("Residence") == null)
            return true;
        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(block.getLocation());
        if (res == null || res.isOwner(player)) {
            return true;
        }
        return res.getPermissions().playerHas(player, player.getWorld().toString(), Flags.container, false) || Residence.getInstance().isResAdminOn(player);

    }

    @Override
    public Boolean protectBlock(Player player, Block block) {
        return false;
    }
}
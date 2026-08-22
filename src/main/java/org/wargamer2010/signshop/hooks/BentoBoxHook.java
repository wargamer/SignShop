package org.wargamer2010.signshop.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.wargamer2010.signshop.player.SignShopPlayer;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.RanksManager;

import java.util.Optional;

/**
 * BentoBox integration hook that checks island membership for skyblock/gamemode addon protection.
 */
public class BentoBoxHook implements Hook {
    @Override
    public String getName() {
        return "BentoBox";
    }

    @Override
    public Boolean canBuild(Player player, Block block) {
        if (HookManager.getHook("BentoBox") == null) {
            return true;
        }

        //BentoBox does not currently have a general bypass for all gamemodes, so we will check for op/superadmin
        if (SignShopPlayer.isOp(player)) {
            return true;
        }

        BentoBox bentoBox = BentoBox.getInstance();

        // Checks if bentobox operates in given world.
        if (bentoBox.getIWM().inWorld(block.getWorld())) {
            // Get the island at given location.
            Optional<Island> island = bentoBox.getIslands().getIslandAt(block.getLocation());

            // Returns true only if island at the location exist, and player is a member of the island
            return island.isPresent() &&
                    island.get().getMemberSet(RanksManager.MEMBER_RANK).contains(player.getUniqueId());
        }

        // If bentobox does not operate in given world then return true?
        return true;
    }

    @Override
    public Boolean protectBlock(Player player, Block block) {
        return false;
    }
}

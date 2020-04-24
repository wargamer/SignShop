package org.wargamer2010.signshop.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.wargamer2010.signshop.player.SignShopPlayer;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;

import java.util.Optional;


public class BentoBoxHook implements Hook {
    @Override
    public String getName() {
        return "BentoBox";
    }

    @Override //TODO Make use of bentobox flags
    public Boolean canBuild(Player player, Block block) {
        if (HookManager.getHook("BentoBox") == null) {
            return true;
        }
        //BentoBox does not currently have a general bypass for all gamemodes so we will check for op/superadmin
        if (SignShopPlayer.isOp(player)) {
            return true;
        }
        BentoBox bentoBox = BentoBox.getInstance();
        User user = User.getInstance(player);
        IslandsManager islandsManager = bentoBox.getIslands();
        Optional<Island> island = islandsManager.getIslandAt(block.getLocation());
        if (island.isPresent() && island.get().getMembers().containsKey(user.getUniqueId())) {
            //visitor:0 coop:200 trusted:400 member:500 sub-owner:900 owner:1000
            return island.get().getMembers().get(user.getUniqueId()) >= 500; // visitor:0 coop:
        }
        return false;
    }

    @Override
    public Boolean protectBlock(Player player, Block block) {
        return false;
    }
}

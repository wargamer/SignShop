package org.wargamer2010.signshop.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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

    @Override
    public Boolean canBuild(Player player, Block block) {
        if(HookManager.getHook("BentoBox") == null)
            return true;
        BentoBox bentoBox = BentoBox.getInstance();
        User user = User.getInstance(player);
        IslandsManager islandsManager = bentoBox.getIslands();
        Optional<Island> island = islandsManager.getIslandAt(block.getLocation());
        if (island.isPresent() && island.get().getMembers().containsKey(user.getUniqueId())){
            return  island.get().getMembers().get(user.getUniqueId()) >= 500;
        }
        return false;
    }

    @Override
    public Boolean protectBlock(Player player, Block block) {
        return false;
    }
}

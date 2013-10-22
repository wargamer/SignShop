
package org.wargamer2010.signshop.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import com.github.derwisch.loreLocks.LoreLocks;
import com.github.derwisch.loreLocks.Permissions;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LorelocksHook implements Hook {

    @Override
    public String getName() {
        return "Lorelocks";
    }

    @Override
    public Boolean canBuild(Player player, Block block) {
        if(HookManager.getHook("Lorelocks") == null)
            return true;
        Material blockMat = block.getType();
        if(blockMat != Material.getMaterial("CHEST") && blockMat != Material.getMaterial("TRAPPED_CHEST"))
            return true;

        Chest chest = (Chest)block.getState();
        ItemStack lock = chest.getInventory().getItem(0);
        if(lock == null)
            return true;
        ItemMeta lockMeta = lock.getItemMeta();
        if(lockMeta == null)
            return true;
        List<String> lockLore = lockMeta.getLore();
        if(lockLore == null)
            return true;

        if(LoreLocks.instance.isLock(lock) && !LoreLocks.instance.playerHasKey(player, lock)) {
            // If the player needs to pick the lock, he shouldn't be allowed to link it
            // If he has the bypass permission, he's probably an admin
            if(!player.hasPermission(Permissions.BYPASS))
                return false;
        }

        return true;
    }

    @Override
    public Boolean protectBlock(Player player, Block block) {
        return false;
    }
}

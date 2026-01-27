package org.wargamer2010.signshop.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Interface for protection plugin integrations.
 *
 * <p>Hooks allow SignShop to respect protection plugins like WorldGuard, Towny,
 * GriefPrevention, etc. Before creating shops or linking blocks, SignShop
 * checks all registered hooks to verify the player has permission.</p>
 *
 * @see HookManager
 */
public interface Hook {
    String getName();

    Boolean canBuild(Player player, Block block);

    Boolean protectBlock(Player player, Block block);
}

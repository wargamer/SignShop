package org.wargamer2010.signshop.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.material.Attachable;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.events.SSDestroyedEvent;
import org.wargamer2010.signshop.events.SSDestroyedEventType;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SignShopBlockListener implements Listener {

    private List<Block> getAttachables(Block from) {
        List<Block> attachables = new ArrayList<>();
        List<BlockFace> checkFaces = new ArrayList<>();
        checkFaces.add(BlockFace.UP);
        checkFaces.add(BlockFace.NORTH);
        checkFaces.add(BlockFace.EAST);
        checkFaces.add(BlockFace.SOUTH);
        checkFaces.add(BlockFace.WEST);

        for(BlockFace face : checkFaces) {
            if(from.getRelative(face).getState().getData() instanceof Attachable) {
                Attachable att = (Attachable)from.getRelative(face).getState().getData();
                if(from.getRelative(face).getRelative(att.getAttachedFace()).equals(from))
                    attachables.add(from.getRelative(face));
            }
        }

        return attachables;
    }

    private boolean canNotBreakBlock(Block block, Player player, boolean recurseOverAttachables) {
        Map<Seller, SSDestroyedEventType> affectedSellers = signshopUtil.getRelatedShopsByBlock(block);
        SignShopPlayer ssPlayer = new SignShopPlayer(player);

        for(Map.Entry<Seller, SSDestroyedEventType> destroyal : affectedSellers.entrySet()) {
            SSDestroyedEvent event = new SSDestroyedEvent(block, ssPlayer, destroyal.getKey(), destroyal.getValue());
            SignShop.scheduleEvent(event);
            if(event.isCancelled())
                return true;
        }

        if(recurseOverAttachables) {
            for(Block attached : getAttachables(block)) {
                if (canNotBreakBlock(attached, player, false))
                    return true;
            }
        }

        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        // We want to run at the Highest level so we can tell if other plugins cancelled the event
        // But we don't want to run at Monitor since we want to be able to cancel the event ourselves
        if(event.isCancelled())
            return;
        if (canNotBreakBlock(event.getBlock(), event.getPlayer(), true))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBurn(BlockBurnEvent event) {
        if(event.isCancelled())
            return;

        if (canNotBreakBlock(event.getBlock(), null, true))
            event.setCancelled(true);
    }
}

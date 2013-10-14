package org.wargamer2010.signshop.listeners;

import java.util.ArrayList;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.material.Attachable;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.events.SSDestroyedEvent;
import org.wargamer2010.signshop.events.SSDestroyedEventType;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

public class SignShopBlockListener implements Listener {

    private List<Seller> getShopsFromMiscSetting(String miscname, Block pBlock) {
        List<Block> shopsWithBlockInMisc = Storage.get().getShopsWithMiscSetting(miscname, signshopUtil.convertLocationToString(pBlock.getLocation()));
        List<Seller> sellers = new LinkedList<Seller>();
        if(!shopsWithBlockInMisc.isEmpty()) {
            for(Block block : shopsWithBlockInMisc) {
                sellers.add(Storage.get().getSeller(block.getLocation()));
            }
        }
        return sellers;
    }

    private List<Block> getAttachables(Block from) {
        List<Block> attachables = new ArrayList<Block>();
        List<BlockFace> checkFaces = new ArrayList<BlockFace>();
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

    private boolean canBreakBlock(Block block, Player player) {
        Map<Seller, SSDestroyedEventType> affectedSellers = new LinkedHashMap<Seller, SSDestroyedEventType>();
        SignShopPlayer ssPlayer = new SignShopPlayer(player);

        if(Storage.get().getSeller(block.getLocation()) != null)
            affectedSellers.put(Storage.get().getSeller(block.getLocation()), SSDestroyedEventType.sign);
        if(itemUtil.clickedSign(block)) {
            for(Seller seller : getShopsFromMiscSetting("sharesigns", block))
                affectedSellers.put(seller, SSDestroyedEventType.miscblock);
            for(Seller seller : getShopsFromMiscSetting("restrictedsigns", block))
                affectedSellers.put(seller, SSDestroyedEventType.miscblock);
        }
        for(Seller seller : Storage.get().getShopsByBlock(block))
            affectedSellers.put(seller, SSDestroyedEventType.attachable);

        for(Map.Entry<Seller, SSDestroyedEventType> destroyal : affectedSellers.entrySet()) {
            SSDestroyedEvent event = new SSDestroyedEvent(block, ssPlayer, destroyal.getKey(), destroyal.getValue());
            SignShop.scheduleEvent(event);
            if(event.isCancelled())
                return false;
        }

        for(Block attached : getAttachables(block)) {
            if(!canBreakBlock(attached, player))
                return false;
        }

        return true;
    }

    public boolean clearAttachedEntities(Block block, SignShopPlayer player) {
        if(player != null && player.isOp())
            return false;
        List<BlockFace> checkFaces = new ArrayList<BlockFace>();
        checkFaces.add(BlockFace.UP);
        checkFaces.add(BlockFace.NORTH);
        checkFaces.add(BlockFace.EAST);
        checkFaces.add(BlockFace.SOUTH);
        checkFaces.add(BlockFace.WEST);

        boolean foundOne = false;
        for(Entity ent : block.getWorld().getEntities()) {
            if(ent.getType() == EntityType.valueOf("ITEM_FRAME")) {
                for(BlockFace face : checkFaces) {
                    if(signshopUtil.roughLocationCompare(block.getRelative(face).getLocation(), ent.getLocation())) {
                        if(!Storage.get().getShopsWithMiscSetting("itemframelocation", signshopUtil.convertLocationToString(ent.getLocation())).isEmpty()) {
                            // The frame is attached to a shop, player is not OP and it will be broken
                            ItemFrame frame = (ItemFrame)ent;
                            // So clean it to prevent an exploit
                            frame.setItem(null);
                            foundOne = true;
                        }
                    }
                }
            }
        }

        return foundOne;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        // We want to run at the Highest level so we can tell if other plugins cancelled the event
        // But we don't want to run at Monitor since we want to be able to cancel the event ourselves
        if(event.isCancelled())
            return;
        if(!canBreakBlock(event.getBlock(), event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBurn(BlockBurnEvent event) {
        if(event.isCancelled())
            return;

        if(!canBreakBlock(event.getBlock(), null))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreakMonitor(BlockBreakEvent event) {
        if(event.isCancelled())
            return;
        SignShopPlayer player = event.getPlayer() == null ? null : new SignShopPlayer(event.getPlayer());
        clearAttachedEntities(event.getBlock(), player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPistonMoveMonitor(BlockPistonExtendEvent event) {
        if(event.isCancelled())
            return;
        Block facing = event.getBlock().getRelative(event.getDirection());
        if(facing.getType() == Material.getMaterial("AIR"))
            return;

        clearAttachedEntities(facing, null);
    }
}

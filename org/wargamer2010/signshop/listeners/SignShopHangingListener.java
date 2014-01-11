package org.wargamer2010.signshop.listeners;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.events.SSDestroyedEvent;
import org.wargamer2010.signshop.events.SSDestroyedEventType;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.clicks;
import org.wargamer2010.signshop.util.signshopUtil;

public class SignShopHangingListener implements Listener {

    /**
     * Attempts to fix the location when the frame was moved by means of a piston
     *
     * @param frame
     * @return
     */
    private Location getLastLocation(ItemFrame frame) {
        Block block = frame.getLocation().getWorld()
                .getBlockAt(frame.getLocation()).getRelative(frame.getAttachedFace());
        // The piston extended and the frame used to be where the piston is now
        return block.getLocation();
    }

    private boolean shopHasItemframe(Location loc) {
        return !getShopsWithItemframe(loc).isEmpty();
    }

    private List<Block> getShopsWithItemframe(Location loc) {
        return Storage.get().getShopsWithMiscSetting("itemframelocation", signshopUtil.convertLocationToString(loc));
    }

    private void fireDestroyedEvent(ItemFrame frame, SignShopPlayer player) {
        if(frame == null)
            return;

        frame.setItem(null);

        for(Block block : getShopsWithItemframe(frame.getLocation())) {
            Seller seller = Storage.get().getSeller(block.getLocation());

            SSDestroyedEvent event = new SSDestroyedEvent(frame.getLocation().getBlock(), player,
                    seller, SSDestroyedEventType.miscblock);
            event.setCanBeCancelled(false);
            SignShop.scheduleEvent(event);
        }
    }

    private void clearAttachedEntities(Block block) {
        List<BlockFace> checkFaces = new ArrayList<BlockFace>();
        checkFaces.add(BlockFace.UP);
        checkFaces.add(BlockFace.NORTH);
        checkFaces.add(BlockFace.EAST);
        checkFaces.add(BlockFace.SOUTH);
        checkFaces.add(BlockFace.WEST);

        for(Entity ent : block.getWorld().getEntities())
            if(ent.getType() == EntityType.valueOf("ITEM_FRAME"))
                for(BlockFace face : checkFaces)
                    if(signshopUtil.roughLocationCompare(block.getRelative(face).getLocation(), ent.getLocation()))
                        if(shopHasItemframe(ent.getLocation())) // Clean it up to prevent an exploit
                            fireDestroyedEvent((ItemFrame)ent, null);
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onHangingBreak(HangingBreakEvent event) {
        if(event.isCancelled())
            return;

        if(event instanceof HangingBreakByEntityEvent) {
            onHangingBreakByEntity((HangingBreakByEntityEvent)event);
            return;
        }

        if(event.getEntity().getType() == EntityType.valueOf("ITEM_FRAME")) {
            Location loc = getLastLocation(((ItemFrame)event.getEntity()));
            ItemFrame frame = ((ItemFrame)event.getEntity());
            if(shopHasItemframe(loc) || shopHasItemframe(frame.getLocation()))
                fireDestroyedEvent(frame, null);
        }
    }

    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if(event.isCancelled() || Material.getMaterial("ITEM_FRAME") == null)
            return;
        if(event.getRemover() == null || !(event.getRemover() instanceof Player)) {
            if(shopHasItemframe(event.getEntity().getLocation()))
                event.setCancelled(true); // Creeper explosion, no player to work with
            return;
        }

        Player player = (Player)event.getRemover();
        SignShopPlayer ssPlayer = new SignShopPlayer(player);

        if(player.getItemInHand() == null || player.getItemInHand().getType() != SignShopConfig.getLinkMaterial()) {
            if(!Storage.get().getShopsWithMiscSetting("itemframelocation", signshopUtil.convertLocationToString(event.getEntity().getLocation())).isEmpty()) {
                if(!ssPlayer.isOp())
                    event.setCancelled(true); // Breaking the frame while being linked to a shop causes an exploit
                else if(event.getEntity() instanceof ItemFrame)
                    fireDestroyedEvent(((ItemFrame)event.getEntity()), ssPlayer);
            }
            return;
        }

        event.setCancelled(true);

        if(!signshopUtil.clickedSignShopMat(event.getEntity(), ssPlayer))
            return;

        if(clicks.mClicksPerEntity.containsKey(event.getEntity())) {
            ssPlayer.sendMessage(SignShopConfig.getError("deselected_hanging", null));
            clicks.mClicksPerEntity.remove(event.getEntity());
        } else {
            ssPlayer.sendMessage(SignShopConfig.getError("selected_hanging", null));
            clicks.mClicksPerEntity.put(event.getEntity(), ssPlayer.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        if(event.isCancelled())
            return;

        if(Material.getMaterial("ITEM_FRAME") != null && event.getRightClicked() instanceof ItemFrame) {
            SignShopPlayer ssPlayer = new SignShopPlayer(event.getPlayer());
            if(ssPlayer.isOp())
                return;

            List<Block> shops = Storage.get().getShopsWithMiscSetting("itemframelocation", signshopUtil.convertLocationToString(event.getRightClicked().getLocation()));
            if(shops.isEmpty())
                return;

            for(Block block : shops) {
                Seller seller = Storage.get().getSeller(block.getLocation());
                if(seller.getOwner().equals(ssPlayer.getName()))
                    return;
            }
            event.setCancelled(true);
            ssPlayer.sendMessage(SignShopConfig.getError("not_allowed_to_rotate_frame", null));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonMove(BlockPistonExtendEvent event) {
        if(event.isCancelled())
            return;
        // This is not covered by the Hanging events

        Block lastBlock = event.getBlock();
        boolean foundAir = false;

        if(lastBlock.getType() == Material.getMaterial("AIR"))
            return;

        int maximum = 12;
        int current = 0;

        while(!foundAir) {
            // A piston should only be able to push up to 12 blocks
            // Source: http://minecraft.gamepedia.com/Piston
            if(current > maximum)
                break;
            Block block = lastBlock.getRelative(event.getDirection());
            if(block.getType() == Material.getMaterial("AIR"))
                foundAir = true;
            else
                lastBlock = block;
            current++;
        }

        if(foundAir)
            clearAttachedEntities(lastBlock);
    }
}

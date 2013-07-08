package org.wargamer2010.signshop.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.clicks;
import org.wargamer2010.signshop.util.signshopUtil;

public class SignShopHangingListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if(event.getRemover() == null || !(event.getRemover() instanceof Player) || event.isCancelled() || Material.getMaterial("ITEM_FRAME") == null)
            return;
        Player player = (Player)event.getRemover();
        SignShopPlayer ssPlayer = new SignShopPlayer(player);

        if(player.getItemInHand() == null || player.getItemInHand().getType() != SignShopConfig.getLinkMaterial()) {
            if(!Storage.get().getShopsWithMiscSetting("itemframelocation", signshopUtil.convertLocationToString(event.getEntity().getLocation())).isEmpty()) {
                if(!ssPlayer.isOp())
                    event.setCancelled(true); // Breaking the frame while being linked to a shop causes an exploit
                else if(event.getEntity() instanceof ItemFrame)
                    ((ItemFrame)event.getEntity()).setItem(null);
            }
            return;
        }

        event.setCancelled(true);
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
        SignShopPlayer ssPlayer = new SignShopPlayer(event.getPlayer());
        if(ssPlayer.isOp())
            return;
        if(Material.getMaterial("ITEM_FRAME") != null && event.getRightClicked() instanceof ItemFrame) {
            for(Block block : Storage.get().getShopsWithMiscSetting("itemframelocation", signshopUtil.convertLocationToString(event.getRightClicked().getLocation()))) {
                Seller seller = Storage.get().getSeller(block.getLocation());
                if(seller.getOwner().equals(ssPlayer.getName()))
                    return;
            }
            event.setCancelled(true);
            ssPlayer.sendMessage(SignShopConfig.getError("not_allowed_to_rotate_frame", null));
        }
    }
}

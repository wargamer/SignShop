
package org.wargamer2010.signshop.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.events.SSEventFactory;
import org.wargamer2010.signshop.events.SSLinkEvent;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.clicks;
import org.wargamer2010.signshop.util.signshopUtil;

public class SignShopHangingListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if(event.getRemover() == null || !(event.getRemover() instanceof Player))
            return;
        Player player = (Player)event.getRemover();
        SignShopPlayer ssPlayer = new SignShopPlayer(player);

        if(player.getItemInHand() == null || player.getItemInHand().getType() != SignShopConfig.getLinkMaterial()) {
            if(!Storage.get().getShopsWithMiscSetting("itemframelocation", signshopUtil.convertLocationToString(event.getEntity().getLocation())).isEmpty() && !ssPlayer.isOp())
                event.setCancelled(true); // Breaking the frame while being linked to a shop causes an exploit
            return;
        }

        event.setCancelled(true);
        if(clicks.mClicksPerEntity.containsKey(event.getEntity())) {
            ssPlayer.sendMessage("You have deselected a Hanging item.");
            clicks.mClicksPerEntity.remove(event.getEntity());
        } else {
            ssPlayer.sendMessage("You have selected a Hanging item.");
            clicks.mClicksPerEntity.put(event.getEntity(), ssPlayer.getPlayer());
        }
    }
}

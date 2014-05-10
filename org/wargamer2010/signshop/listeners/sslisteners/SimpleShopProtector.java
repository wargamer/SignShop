
package org.wargamer2010.signshop.listeners.sslisteners;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.InventoryHolder;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.events.SSDestroyedEvent;
import org.wargamer2010.signshop.events.SSDestroyedEventType;
import org.wargamer2010.signshop.hooks.HookManager;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

public class SimpleShopProtector implements Listener {
    private Boolean canDestroy(Player player, Block bBlock) {
        SignShopPlayer ssPlayer = new SignShopPlayer(player);
        if(itemUtil.clickedSign(bBlock)) {
            Seller seller = Storage.get().getSeller(bBlock.getLocation());
            if(seller == null || seller.isOwner(ssPlayer) || SignShopPlayer.isOp(player) || !SignShopConfig.getEnableShopOwnerProtection())
                return true;
            else
                return false;
        }
        return true;
    }

    private void cleanUpMiscStuff(String miscname, Block block) {
        List<Block> shopsWithSharesign = Storage.get().getShopsWithMiscSetting(miscname, signshopUtil.convertLocationToString(block.getLocation()));
        for(Block bTemp : shopsWithSharesign) {
            Seller seller = Storage.get().getSeller(bTemp.getLocation());
            String temp = seller.getMisc().get(miscname);
            temp = temp.replace(signshopUtil.convertLocationToString(block.getLocation()), "");
            temp = temp.replace(SignShopArguments.seperator+SignShopArguments.seperator, SignShopArguments.seperator);
            if(temp.length() > 0) {
                if(temp.endsWith(SignShopArguments.seperator))
                    temp = temp.substring(0, temp.length()-1);
                if(temp.length() > 1 && temp.charAt(0) == SignShopArguments.seperator.charAt(0))
                    temp = temp.substring(1, temp.length());
            }
            if(temp.length() == 0)
                seller.getMisc().remove(miscname);
            else
                seller.getMisc().put(miscname, temp);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSDestroyEvent(SSDestroyedEvent event) {
        if(event.isCancelled() || !event.canBeCancelled())
            return;

        // Check if the shop is being destroyed by something other than a player
        // If that is the case, we'd like to cancel it as shops shouldn't burn to the ground
        if(event.getPlayer().getPlayer() == null) {
            event.setCancelled(true);
            return;
        }

        SignShopPlayer player = event.getPlayer();

        if(player.getPlayer().getGameMode() == GameMode.CREATIVE
                && SignShopConfig.getProtectShopsInCreative()
                && (player.getItemInHand() == null || player.getItemInHand().getType() != SignShopConfig.getDestroyMaterial())) {
            event.setCancelled(true);

            if(event.getShop().isOwner(player) || event.getPlayer().isOp()) {
                Map<String, String> temp = new LinkedHashMap<String, String>();
                temp.put("!destroymaterial", signshopUtil.capFirstLetter(SignShopConfig.getDestroyMaterial().name().toLowerCase()));

                event.getPlayer().sendMessage(SignShopConfig.getError("use_item_to_destroy_shop", temp));
            }
            return;
        }

        Boolean bCanDestroy = canDestroy(player.getPlayer(), event.getBlock());
        if(!bCanDestroy)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSSDestroyCleanup(SSDestroyedEvent event) {
        if(event.isCancelled())
            return;

        if(event.getReason() == SSDestroyedEventType.sign) {
            if(event.getShop() != null && event.getShop().getSign() != null)
                itemUtil.setSignStatus(event.getShop().getSign(), ChatColor.BLACK);
            Storage.get().removeSeller(event.getBlock().getLocation());
        } else if(event.getReason() == SSDestroyedEventType.miscblock) {
            cleanUpMiscStuff("sharesigns", event.getBlock());
            cleanUpMiscStuff("restrictedsigns", event.getBlock());
        } else if(event.getReason() == SSDestroyedEventType.attachable) {
            // More shops might be attached to this attachable, but the event will be fired multiple times
            if(event.getBlock() != null && event.getBlock().getState() instanceof InventoryHolder) {
                // If a chest is broken, we can no longer safely assume the shop will continue working
                // So we can't just unlink it from the shop as it might completely break it's functionality
                if(event.getShop() != null && event.getShop().getSign() != null) {
                    itemUtil.setSignStatus(event.getShop().getSign(), ChatColor.BLACK);
                    Storage.get().removeSeller(event.getShop().getSignLocation());
                }
            }
            // No need to remove the seller as we can't safely assume breaking anything else than a chest will make the shop useless
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSSCreatedEvent(SSCreatedEvent event) {
        if(event.isCancelled() || !SignShopConfig.getEnableAutomaticLock())
            return;

        for(Block containable : event.getContainables()) {
            if(HookManager.protectBlock(event.getPlayer().getPlayer(), containable))
                event.getPlayer().sendMessage(SignShopConfig.getError("shop_is_now_protected", event.getMessageParts()));
        }

    }
}

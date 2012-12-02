
package org.wargamer2010.signshop.listeners.sslisteners;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.events.SSDestroyedEvent;
import org.wargamer2010.signshop.events.SSDestroyedEventType;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

public class SimpleShopProtector implements Listener {
    private Block getBlockAttachedTo(Block bBlock) {
        if(bBlock.getType() == Material.getMaterial("WALL_SIGN")) {
            org.bukkit.material.Sign sign = (org.bukkit.material.Sign)bBlock.getState().getData();
            return bBlock.getRelative(sign.getAttachedFace());
        } else
            return null;
    }

    private Boolean checkSign(Block bBlock, Block bDestroyed, BlockFace bf, Player player) {
        if(bBlock.getType() == Material.getMaterial("SIGN_POST") && bf.equals(BlockFace.UP))
            return (canDestroy(player, bBlock, false));
        else if(bBlock.getType() == Material.getMaterial("WALL_SIGN") && getBlockAttachedTo(bBlock).equals(bDestroyed))
            return (canDestroy(player, bBlock, false));
        else
            return true;
    }

    private Boolean canDestroy(Player player, Block bBlock, Boolean firstcall) {
        if(itemUtil.clickedSign(bBlock)) {
            Seller seller = Storage.get().getSeller(bBlock.getLocation());
            if(seller == null || (seller != null && (seller.getOwner().equals(player.getName()) || player.isOp()))) {
                Storage.get().removeSeller(bBlock.getLocation());
                return true;
            } else
                return false;
        }
        if(firstcall) {
            Block bSign = null;
            List<BlockFace> checkFaces = new ArrayList<BlockFace>();
            checkFaces.add(BlockFace.UP);
            checkFaces.add(BlockFace.NORTH);
            checkFaces.add(BlockFace.EAST);
            checkFaces.add(BlockFace.SOUTH);
            checkFaces.add(BlockFace.WEST);
            for(int i = 0; i < checkFaces.size(); i++)
                if(checkSign(bBlock.getRelative(checkFaces.get(i)), bBlock, checkFaces.get(i), player))
                    bSign = bBlock.getRelative(checkFaces.get(i));
                else
                    return false;
            if(bSign != null)
                Storage.get().removeSeller(bSign.getLocation());
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
        if(event.isCancelled())
            return;

        // Check if the shop is being destroyed by something other than a player
        // If that is the case, we'd like to cancel it as shops shouldn't burn to the ground
        if(event.getPlayer().getPlayer() == null)
            event.setCancelled(true);

        Player player = event.getPlayer().getPlayer();

        if(player.getItemInHand() != null
                && player.getItemInHand().getType() == SignShopConfig.getLinkMaterial()
                && player.getGameMode() == GameMode.CREATIVE
                && SignShopConfig.getProtectShopsInCreative()) {
            event.setCancelled(true);
            return;
        }

        Boolean bCanDestroy = canDestroy(player, event.getBlock(), true);
        if(!bCanDestroy)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSSDestroyCleanup(SSDestroyedEvent event) {
        if(event.isCancelled())
            return;
        // Does the seller still exist?
        if(Storage.get().getSignFromSeller(event.getShop()) == null)
            return;

        if(event.getReason() == SSDestroyedEventType.sign) {
            Storage.get().removeSeller(event.getBlock().getLocation());
        } else if(event.getReason() == SSDestroyedEventType.miscblock) {
            cleanUpMiscStuff("sharesigns", event.getBlock());
            cleanUpMiscStuff("restrictedsigns", event.getBlock());
        } else if(event.getReason() == SSDestroyedEventType.attachable) {
            // More shops might be attached to this attachable, but the event will be fired multiple times
            // if that's the case
            Block bSign = Storage.get().getSignFromSeller(event.getShop());
            if(itemUtil.clickedSign(bSign)) {
                itemUtil.setSignStatus(bSign, ChatColor.BLACK);
                Storage.get().removeSeller(bSign.getLocation());
            }
        }
    }
}

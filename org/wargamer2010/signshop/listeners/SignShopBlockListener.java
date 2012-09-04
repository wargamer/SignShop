package org.wargamer2010.signshop.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

public class SignShopBlockListener implements Listener {
    
    private Block getBlockAttachedTo(Block bBlock) {
        if(bBlock.getType() == Material.WALL_SIGN) {
            org.bukkit.material.Sign sign = (org.bukkit.material.Sign)bBlock.getState().getData();             
            return bBlock.getRelative(sign.getAttachedFace());
        } else
            return null;
    }
    
    private Boolean checkSign(Block bBlock, Block bDestroyed, BlockFace bf, Player player) {
        if(bBlock.getType() == Material.SIGN_POST && bf.equals(BlockFace.UP))
            return (canDestroy(player, bBlock, false));        
        else if(bBlock.getType() == Material.WALL_SIGN && getBlockAttachedTo(bBlock).equals(bDestroyed))
            return (canDestroy(player, bBlock, false));
        else
            return true;
    }
    
    private Boolean canDestroy(Player player, Block bBlock, Boolean firstcall) { 
        if(bBlock.getType() == Material.SIGN_POST || bBlock.getType() == Material.WALL_SIGN) {
            Seller seller = SignShop.Storage.getSeller(bBlock.getLocation());        
            if(seller == null || (seller != null && (seller.getOwner().equals(player.getName()) || player.isOp()))) {
                SignShop.Storage.removeSeller(bBlock.getLocation());
                return true;
            } else
                return false;
        }
        if(firstcall) {            
            Block bSign = null;
            List<BlockFace> checkFaces = new ArrayList();            
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
                SignShop.Storage.removeSeller(bSign.getLocation());
        }
        return true;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {        
        if(event.getPlayer().getItemInHand() != null 
                && event.getPlayer().getItemInHand().getType() == Material.REDSTONE
                && event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            event.setCancelled(true);
            return;
        }
        Boolean bCanDestroy = canDestroy(event.getPlayer(), event.getBlock(), true);
        if(!bCanDestroy)
            event.setCancelled(true);        
        if(!event.isCancelled() && event.getBlock() instanceof InventoryHolder) {            
            List<Block> signs = SignShop.Storage.getSignsFromHolder(event.getBlock());
            if(signs != null)
                for (Block temp : signs) {
                    SignShop.Storage.removeSeller(temp.getLocation());
                    itemUtil.setSignStatus(temp, ChatColor.BLACK);
                }
            return;
        } else if(!event.isCancelled() && itemUtil.clickedSign(event.getBlock())) {
            List<Block> shopsWithSharesign = SignShop.Storage.getShopsWithMiscSetting("sharesigns", signshopUtil.convertLocationToString(event.getBlock().getLocation()));
            for(Block bTemp : shopsWithSharesign) {
                Seller seller = SignShop.Storage.getSeller(bTemp.getLocation());
                String temp = seller.getMisc().get("sharesigns");
                temp = temp.replace(signshopUtil.convertLocationToString(event.getBlock().getLocation()), "");
                temp = temp.replace(SignShopArguments.seperator+SignShopArguments.seperator, SignShopArguments.seperator);
                if(temp.length() > 0) {
                    if(temp.endsWith(SignShopArguments.seperator))
                        temp = temp.substring(0, temp.length()-1);
                    if(temp.length() > 1 && temp.charAt(0) == SignShopArguments.seperator.charAt(0))
                        temp = temp.substring(1, temp.length());
                }
                if(temp.length() == 0)
                    seller.getMisc().remove("sharesigns");
                else
                    seller.getMisc().put("sharesigns", temp);
            }
            SignShop.Storage.DelayedSave();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBurn(BlockBurnEvent event){
        if(event.getBlock().getType() == Material.WALL_SIGN
        || event.getBlock().getType() == Material.SIGN_POST){
            SignShop.Storage.removeSeller(event.getBlock().getLocation());
        } else if(event.getBlock() instanceof InventoryHolder) {
            List<Block> signs = SignShop.Storage.getSignsFromHolder(event.getBlock());
            if(signs != null)
                for (Block temp : signs) {                    
                    SignShop.Storage.removeSeller(temp.getLocation());
                    itemUtil.setSignStatus(temp, ChatColor.BLACK);
                }
        }
    }
}
